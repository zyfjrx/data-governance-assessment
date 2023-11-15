package com.zhang.dga.governance.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.zhang.dga.governance.bean.GovernanceAssessDetail;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 治理考评结果明细 Mapper 接口
 * </p>
 *
 * @author zhangyf
 * @since 2023-11-14
 */
@Mapper
@DS("dga")
public interface GovernanceAssessDetailMapper extends BaseMapper<GovernanceAssessDetail> {

}
