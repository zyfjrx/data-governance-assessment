package com.zhang.dga.meta.service.impl;

import com.alibaba.fastjson.JSON;
import com.zhang.dga.meta.bean.TableMetaInfo;
import com.zhang.dga.meta.mapper.TableMetaInfoMapper;
import com.zhang.dga.meta.service.TableMetaInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
import org.apache.hadoop.hive.metastore.IMetaStoreClient;
import org.apache.hadoop.hive.metastore.api.FieldSchema;
import org.apache.hadoop.hive.metastore.api.MetaException;
import org.apache.hadoop.hive.metastore.api.Order;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.hadoop.hive.metastore.conf.MetastoreConf;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 元数据表 服务实现类
 * </p>
 *
 * @author zhangyf
 * @since 2023-11-08
 */
@Service
public class TableMetaInfoServiceImpl extends ServiceImpl<TableMetaInfoMapper, TableMetaInfo> implements TableMetaInfoService {
    @Value("${hive.meta-server.url}") // 容器启动时执行
    String hiveMetaServerUrl = null;


    IMetaStoreClient hiveClient;

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
        List<String> allTableNameList = hiveClient.getAllTables(schemaName);
        System.out.println("allTables" + allTableNameList);
        ArrayList<TableMetaInfo> tableMetaInfos = new ArrayList<>(allTableNameList.size());
        for (String tableName : allTableNameList) {
            Table table = hiveClient.getTable(schemaName, tableName);
            System.out.println(table);
            TableMetaInfo tableMetaInfo = new TableMetaInfo();
            tableMetaInfo.setSchemaName(schemaName);
            tableMetaInfo.setTableName(tableName);

            // 列转换
            List<FieldSchema> fieldList = table.getSd().getCols();
            tableMetaInfo.setColNameJson(JSON.toJSONString(fieldList));

            // 分区列转换
            List<FieldSchema> partitionKeyList = table.getPartitionKeys();
            tableMetaInfo.setPartitionColNameJson(JSON.toJSONString(partitionKeyList));

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
            tableMetaInfo.setTableCreateTime(DateFormatUtils.format(new Date(table.getCreateTime() * 1000L), "yyyy-MM-dd HH:mm:ss"));

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

            tableMetaInfos.add(tableMetaInfo);

        }
        System.out.println(JSON.toJSONString(tableMetaInfos));
    }
}
