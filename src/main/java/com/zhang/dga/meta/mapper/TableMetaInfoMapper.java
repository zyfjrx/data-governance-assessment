package com.zhang.dga.meta.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.zhang.dga.meta.bean.TableMetaInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhang.dga.meta.bean.vo.TableMetaInfoVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
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


    @Select(" select tm.*,te.* ,  te.id  as te_id ,te.create_time as te_create_time \n" +
            "   from table_meta_info tm  join table_meta_info_extra te\n" +
            "             on tm.table_name=te.table_name  and  tm.schema_name=te.schema_name\n" +
            "  where assess_date = (select max(assess_date) from table_meta_info tm1\n" +
            "                   where tm.table_name =tm1.table_name and tm.schema_name =tm1.schema_name  )")
    @ResultMap("tableMetaResultMap")
    public List<TableMetaInfo> selectTableMetaWithExtraList();
}
