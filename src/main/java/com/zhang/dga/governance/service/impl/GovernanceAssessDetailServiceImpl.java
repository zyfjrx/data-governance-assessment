package com.zhang.dga.governance.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhang.dga.common.util.SpringBeanProvider;
import com.zhang.dga.governance.assessor.Assessor;
import com.zhang.dga.governance.bean.GovernanceAssessDetail;
import com.zhang.dga.governance.bean.GovernanceMetric;
import com.zhang.dga.governance.mapper.GovernanceAssessDetailMapper;
import com.zhang.dga.governance.service.GovernanceAssessDetailService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhang.dga.governance.service.GovernanceMetricService;
import com.zhang.dga.meta.bean.TableMetaInfo;
import com.zhang.dga.meta.service.TableMetaInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 治理考评结果明细 服务实现类
 * </p>
 *
 * @author zhangyf
 * @since 2023-11-14
 */
@Service
@DS("dga")
public class GovernanceAssessDetailServiceImpl extends ServiceImpl<GovernanceAssessDetailMapper, GovernanceAssessDetail> implements GovernanceAssessDetailService {
    @Autowired
    TableMetaInfoService tableMetaInfoService;

    @Autowired
    GovernanceMetricService governanceMetricService;

    @Autowired
    SpringBeanProvider springBeanProvider;

    public   void  allMetricAssess(String assessDate){

        //1   查询出 要考评的表（最新的元数据 含辅助信息） List<TableMetaInfo>
        List<TableMetaInfo> tableMetaInfoList=tableMetaInfoService.getTableMetaWithExtraList();

        //如何避免 循环查询数据库？ join


        //2    查询出 要考评的指标列表  List<GovernanceMetric>
        List<GovernanceMetric> governanceMetricList=governanceMetricService.list(new QueryWrapper<GovernanceMetric>().eq("is_disabled","0"));

        for (TableMetaInfo tableMetaInfo : tableMetaInfoList) {
            for (GovernanceMetric governanceMetric : governanceMetricList) {
                Assessor assessor = springBeanProvider.getBean(governanceMetric.getMetricCode(), Assessor.class);
                assessor.metricAssess();
            }
        }

        System.out.println("tableMetaInfoList"+tableMetaInfoList);
        System.out.println("governanceMetricList"+governanceMetricList);

    }
}
