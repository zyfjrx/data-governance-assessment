package com.zhang.dga.meta.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.zhang.dga.meta.bean.TableMetaInfoExtra;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 元数据表附加信息 Mapper 接口
 * </p>
 *
 * @author zhangyf
 * @since 2023-11-09
 */
@Mapper
@DS("dga")
public interface TableMetaInfoExtraMapper extends BaseMapper<TableMetaInfoExtra> {

}
