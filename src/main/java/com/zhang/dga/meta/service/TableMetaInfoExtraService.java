package com.zhang.dga.meta.service;

import com.zhang.dga.meta.bean.TableMetaInfo;
import com.zhang.dga.meta.bean.TableMetaInfoExtra;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 元数据表附加信息 服务类
 * </p>
 *
 * @author zhangyf
 * @since 2023-11-09
 */
public interface TableMetaInfoExtraService extends IService<TableMetaInfoExtra> {
    void initTableMetaExtra(String assessDate, List<TableMetaInfo> tableMetaInfoList);
}
