package com.zhang.dga.meta.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.support.spring.PropertyPreFilters;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhang.dga.common.util.SqlUtil;
import com.zhang.dga.meta.bean.TableMetaInfo;
import com.zhang.dga.meta.bean.TableMetaInfoExtra;
import com.zhang.dga.meta.bean.TableMetaInfoForQuery;
import com.zhang.dga.meta.bean.vo.TableMetaInfoVO;
import com.zhang.dga.meta.mapper.TableMetaInfoMapper;
import com.zhang.dga.meta.service.TableMetaInfoExtraService;
import com.zhang.dga.meta.service.TableMetaInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.HdfsConfiguration;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
import org.apache.hadoop.hive.metastore.IMetaStoreClient;
import org.apache.hadoop.hive.metastore.api.FieldSchema;
import org.apache.hadoop.hive.metastore.api.MetaException;
import org.apache.hadoop.hive.metastore.api.Order;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.hadoop.hive.metastore.conf.MetastoreConf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * <p>
 * 元数据表 服务实现类
 * </p>
 *
 * @author zhangyf
 * @since 2023-11-08
 */
@Service
@DS("dga")
public class TableMetaInfoServiceImpl extends ServiceImpl<TableMetaInfoMapper, TableMetaInfo> implements TableMetaInfoService {
    @Value("${hive.meta-server.url}") // 容器启动时执行
    String hiveMetaServerUrl = null;


    IMetaStoreClient hiveClient;

    @Autowired
    TableMetaInfoExtraService tableMetaInfoExtraService;

    // 初始化 hive 客户端
    @PostConstruct // 后置构造器 容器启动时执行
    private void getHiveMetaStoreClient() {
        HiveConf hiveConf = new HiveConf();
        MetastoreConf.setVar(hiveConf, MetastoreConf.ConfVars.THRIFT_URIS, hiveMetaServerUrl);
        try {
            hiveClient = new HiveMetaStoreClient(hiveConf);
        } catch (MetaException e) {
            throw new RuntimeException(e);
        }

    }

    public void initTableMetaInfo(String assessDate, String schemaName) throws Exception {
        // 清理当日已经存在的数据
        this.remove(new QueryWrapper<TableMetaInfo>().eq("assess_date", assessDate));
        List<String> allTableNameList = hiveClient.getAllTables(schemaName);
        System.out.println("allTables" + allTableNameList);
        ArrayList<TableMetaInfo> tableMetaInfos = new ArrayList<>(allTableNameList.size());
        for (String tableName : allTableNameList) {
            // 提取hive数据
            TableMetaInfo tableMetaInfo = getTableMetaInfoFromHive(schemaName, tableName);
            // 从hdfs中补充数据
            addHdfsInfo(tableMetaInfo);
            // 日期信息
            tableMetaInfo.setAssessDate(assessDate);
            tableMetaInfo.setCreateTime(new Date());
            tableMetaInfos.add(tableMetaInfo);
        }
        saveBatch(tableMetaInfos);

        // 初始化辅助信息表
        tableMetaInfoExtraService.initTableMetaExtra(assessDate, tableMetaInfos);
    }

    // 从hive提取数据
    private TableMetaInfo getTableMetaInfoFromHive(String databaseName, String tableName) {
        TableMetaInfo tableMetaInfo = new TableMetaInfo();
        try {
            Table table = hiveClient.getTable(databaseName, tableName);
            tableMetaInfo.setSchemaName(databaseName);
            tableMetaInfo.setTableName(tableName);

            // 过滤器 用于获取json转换过程中保留字段
            PropertyPreFilters.MySimplePropertyPreFilter filter =
                    new PropertyPreFilters().addFilter("comment", "name", "type");
            // 列转换
            List<FieldSchema> fieldList = table.getSd().getCols();
            tableMetaInfo.setColNameJson(JSON.toJSONString(fieldList, filter));

            // 分区列转换
            List<FieldSchema> partitionKeyList = table.getPartitionKeys();
            tableMetaInfo.setPartitionColNameJson(JSON.toJSONString(partitionKeyList, filter));

            // 作者
            tableMetaInfo.setTableFsOwner(table.getOwner());

            // 参数列转换
            Map<String, String> parameters = table.getParameters();
            tableMetaInfo.setTableParametersJson(JSON.toJSONString(parameters));

            // 表备注
            tableMetaInfo.setTableComment(parameters.get("comment"));

            // 表路径
            tableMetaInfo.setTableFsPath(table.getSd().getLocation());

            // 格式
            tableMetaInfo.setTableOutputFormat(table.getSd().getOutputFormat());
            tableMetaInfo.setTableInputFormat(table.getSd().getInputFormat());
            tableMetaInfo.setTableRowFormatSerde(table.getSd().getSerdeInfo().getSerializationLib());

            // 创建时间
            tableMetaInfo.setTableCreateTime(DateFormatUtils.format(new Date(table.getCreateTime() * 1000L), "yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("Asia/Shanghai")));

            // 表类型
            tableMetaInfo.setTableType(table.getTableType());

            // 桶列转换
            List<String> bucketCols = table.getSd().getBucketCols();
            tableMetaInfo.setTableBucketColsJson(JSON.toJSONString(bucketCols));

            // 桶数量
            tableMetaInfo.setTableBucketNum((long) table.getSd().getNumBuckets());

            // 排序列转换
            List<Order> sortCols = table.getSd().getSortCols();
            tableMetaInfo.setTableSortColsJson(JSON.toJSONString(sortCols));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return tableMetaInfo;
    }

    private void addHdfsInfo(TableMetaInfo tableMetaInfo) {
        String tableFsPath = tableMetaInfo.getTableFsPath();
        String tableFsOwner = tableMetaInfo.getTableFsOwner();
        try {
            FileSystem fs = FileSystem.get(new URI(tableFsPath), new Configuration(), tableFsOwner);
            boolean exists = fs.exists(new Path(tableFsPath));
            if (exists) {
                //求文件大小：  求出所有文件的列表，然后把所有文件的大小汇总相加
                FileStatus[] fileStatuses = fs.listStatus(new Path(tableFsPath));
                addFileInfoRec(fileStatuses, fs, tableMetaInfo);
                tableMetaInfo.setFsCapcitySize(fs.getStatus().getCapacity());
                tableMetaInfo.setFsUsedSize(fs.getStatus().getUsed());
                tableMetaInfo.setFsRemainSize(fs.getStatus().getRemaining());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }


    }

    private void addFileInfoRec(FileStatus[] fileStatuses, FileSystem fs, TableMetaInfo tableMetaInfo) throws IOException {
        for (FileStatus fileStatus : fileStatuses) {
            if (fileStatus.isDirectory()) {
                // 非叶子节点（目录），指向下一个子节点调用自己的方法
                FileStatus[] subFileStatuses = fs.listStatus(fileStatus.getPath());
                addFileInfoRec(subFileStatuses, fs, tableMetaInfo);
            } else {
                // 把文件大小累加到表大小字段
                long fileSize = fileStatus.getLen() + tableMetaInfo.getTableSize();
                tableMetaInfo.setTableSize(fileSize);
                long fileTotalSize = tableMetaInfo.getTableTotalSize() + fileStatus.getLen() * fileStatus.getReplication();
                tableMetaInfo.setTableTotalSize(fileTotalSize);
                // 最后访问时间
                if (tableMetaInfo.getTableLastAccessTime() != null) {
                    if (tableMetaInfo.getTableLastAccessTime().getTime() < fileStatus.getAccessTime()) {
                        tableMetaInfo.setTableLastAccessTime(new Date(fileStatus.getAccessTime()));
                    }
                } else {
                    tableMetaInfo.setTableLastAccessTime(new Date(fileStatus.getAccessTime()));
                }

                // 最后修改时间
                if (tableMetaInfo.getTableLastModifyTime() != null) {
                    if (tableMetaInfo.getTableLastModifyTime().getTime() < fileStatus.getModificationTime()) {
                        tableMetaInfo.setTableLastModifyTime(new Date(fileStatus.getModificationTime()));
                    }
                } else {
                    tableMetaInfo.setTableLastModifyTime(new Date(fileStatus.getModificationTime()));
                }
            }
        }
    }

    // 根据查询条件和分页来进行列表查询
    @Override
    public List<TableMetaInfoVO> getTableMetaInfoList(TableMetaInfoForQuery tableMetaInfoForQuery) {
        StringBuilder sqlSb = new StringBuilder(200);
        sqlSb.append(" select  tm.id ,tm.table_name,tm.schema_name,table_comment,table_size,table_total_size,tec_owner_user_name,busi_owner_user_name, table_last_access_time,table_last_modify_time");
        sqlSb.append(" from table_meta_info tm  join table_meta_info_extra te on tm.table_name=te.table_name and tm.schema_name=te.schema_name");
        sqlSb.append(" where assess_date = (select  max(tm1.assess_date) from table_meta_info  tm1 group by tm1.table_name,tm1.schema_name having tm.schema_name=tm1.schema_name and tm.table_name=tm1.table_name)  ");
        if (tableMetaInfoForQuery.getSchemaName() != null && tableMetaInfoForQuery.getSchemaName().length() > 0) {
            sqlSb.append(" and tm.schema_name like '%").append(SqlUtil.filterUnsafeSql(tableMetaInfoForQuery.getSchemaName())).append("%'");
        }
        if (tableMetaInfoForQuery.getTableName() != null && tableMetaInfoForQuery.getTableName().length() > 0) {
            sqlSb.append(" and table_name like '%").append(SqlUtil.filterUnsafeSql(tableMetaInfoForQuery.getTableName())).append("%'");
        }
        if (tableMetaInfoForQuery.getDwLevel() != null && tableMetaInfoForQuery.getDwLevel().length() > 0) {
            sqlSb.append(" and dw_level like '%").append(SqlUtil.filterUnsafeSql(tableMetaInfoForQuery.getDwLevel())).append("%'");
        }
        // 分页 limit x,x 行号 = （页码 -1）* 每页行数
        int rowNo = (tableMetaInfoForQuery.getPageNo() - 1) * tableMetaInfoForQuery.getPageSize();
        sqlSb.append(" limit " + rowNo + "," + tableMetaInfoForQuery.getPageSize());
        List<TableMetaInfoVO> tableMetaInfoList = this.baseMapper.getTableMetaInfoList(sqlSb.toString());
        return tableMetaInfoList;
    }

    @Override
    public Integer getTableMetaInfoCount(TableMetaInfoForQuery tableMetaInfoForQuery) {
        StringBuilder sqlSb = new StringBuilder(200);
        sqlSb.append(" select  count(*) ");
        sqlSb.append(" from table_meta_info tm  join table_meta_info_extra te on tm.table_name=te.table_name and tm.schema_name=te.schema_name");
        sqlSb.append(" where assess_date = (select  max(tm1.assess_date) from table_meta_info  tm1 group by tm1.table_name,tm1.schema_name having tm.schema_name=tm1.schema_name and tm.table_name=tm1.table_name)  ");
        if (tableMetaInfoForQuery.getSchemaName() != null && tableMetaInfoForQuery.getSchemaName().length() > 0) {
            sqlSb.append(" and tm.schema_name like '%").append(SqlUtil.filterUnsafeSql(tableMetaInfoForQuery.getSchemaName())).append("%'");
        }
        if (tableMetaInfoForQuery.getTableName() != null && tableMetaInfoForQuery.getTableName().length() > 0) {
            sqlSb.append(" and table_name like '%").append(SqlUtil.filterUnsafeSql(tableMetaInfoForQuery.getTableName())).append("%'");
        }
        if (tableMetaInfoForQuery.getDwLevel() != null && tableMetaInfoForQuery.getDwLevel().length() > 0) {
            sqlSb.append(" and dw_level like '%").append(SqlUtil.filterUnsafeSql(tableMetaInfoForQuery.getDwLevel())).append("%'");
        }

        Integer total = this.baseMapper.getTableMetaInfoCount(sqlSb.toString());
        return total;
    }

    @Override
    public TableMetaInfo getTableMetaInfo(Long tableId) {
        TableMetaInfo tableMetaInfo = getById(tableId);
        TableMetaInfoExtra tableMetaInfoExtra = tableMetaInfoExtraService.getOne(new QueryWrapper<TableMetaInfoExtra>()
                .eq("table_name", tableMetaInfo.getTableName())
                .eq("schema_name", tableMetaInfo.getSchemaName())
        );
        tableMetaInfo.setTableMetaInfoExtra(tableMetaInfoExtra);
        return tableMetaInfo;
    }

    @Override
    public List<TableMetaInfo> getTableMetaWithExtraList() {
        return baseMapper.selectTableMetaWithExtraList();
    }
}