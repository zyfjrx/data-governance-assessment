package com.zhang.dga;

import com.zhang.dga.governance.service.GovernanceAssessDetailService;
import com.zhang.dga.meta.service.impl.TableMetaInfoServiceImpl;
import org.apache.hadoop.hive.metastore.api.MetaException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DataGovernanceAssessmentApplicationTests {

    @Autowired
    TableMetaInfoServiceImpl tableMetaInfoService;


    @Autowired
    GovernanceAssessDetailService governanceAssessDetailService;

    @Test
    void contextLoads() {
    }

    @Test
    public void test() throws Exception {
        tableMetaInfoService.initTableMetaInfo("2023-11-08","default");

    }


    @Test
    public void test1() throws Exception {
        governanceAssessDetailService.allMetricAssess("2023-11-14");

    }

}
