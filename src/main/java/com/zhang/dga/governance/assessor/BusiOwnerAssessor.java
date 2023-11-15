package com.zhang.dga.governance.assessor;

import org.springframework.stereotype.Component;

/**
 * @title:
 * @author: zhangyf
 * @date: 2023/11/15 11:13
 **/
@Component("HAS_BUSI_OWNER")
public class BusiOwnerAssessor implements Assessor{
    @Override
    public void metricAssess() {
        System.out.println("是否有业务负责人考评 ");
    }
}
