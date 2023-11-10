package com.zhang.dga.meta.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.zhang.dga.meta.bean.TableMetaInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 元数据表 Mapper 接口
 * </p>
 *
 * @author zhangyf
 * @since 2023-11-08
 */
@Mapper
@DS("dga")
public interface TableMetaInfoMapper extends BaseMapper<TableMetaInfo> {

}
