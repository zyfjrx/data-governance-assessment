package com.zhang.dga.governance.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.zhang.dga.governance.bean.GovernanceMetric;
import com.zhang.dga.governance.mapper.GovernanceMetricMapper;
import com.zhang.dga.governance.service.GovernanceMetricService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 考评指标参数表 服务实现类
 * </p>
 *
 * @author zhangyf
 * @since 2023-11-14
 */
@Service
@DS("dga")
public class GovernanceMetricServiceImpl extends ServiceImpl<GovernanceMetricMapper, GovernanceMetric> implements GovernanceMetricService {

}
