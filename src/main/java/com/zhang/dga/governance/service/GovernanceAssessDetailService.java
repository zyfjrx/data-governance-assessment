package com.zhang.dga.governance.service;

import com.zhang.dga.governance.bean.GovernanceAssessDetail;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 治理考评结果明细 服务类
 * </p>
 *
 * @author zhangyf
 * @since 2023-11-14
 */
public interface GovernanceAssessDetailService extends IService<GovernanceAssessDetail> {
    void allMetricAssess(String assessDate);
}
