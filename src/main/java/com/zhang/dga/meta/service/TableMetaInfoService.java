package com.zhang.dga.meta.service;

import com.zhang.dga.meta.bean.TableMetaInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhang.dga.meta.bean.TableMetaInfoForQuery;
import com.zhang.dga.meta.bean.vo.TableMetaInfoVO;

import java.util.List;

/**
 * <p>
 * 元数据表 服务类
 * </p>
 *
 * @author zhangyf
 * @since 2023-11-08
 */
public interface TableMetaInfoService extends IService<TableMetaInfo> {

    void  initTableMetaInfo(String assessDate,String schemaName) throws  Exception;

    List<TableMetaInfoVO> getTableMetaInfoList(TableMetaInfoForQuery tableMetaInfoForQuery);

    Integer getTableMetaInfoCount(TableMetaInfoForQuery tableMetaInfoForQuery);

    TableMetaInfo getTableMetaInfo(Long tableId);

    List<TableMetaInfo> getTableMetaWithExtraList();
}
