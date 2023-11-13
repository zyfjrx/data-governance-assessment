package com.zhang.dga.meta.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.zhang.dga.meta.bean.TableMetaInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhang.dga.meta.bean.vo.TableMetaInfoVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

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
    @Select("${SQL}")
    List<TableMetaInfoVO> getTableMetaInfoList(@Param("SQL") String sql);

    @Select("${SQL}")
    Integer getTableMetaInfoCount(@Param("SQL") String sql);
}
