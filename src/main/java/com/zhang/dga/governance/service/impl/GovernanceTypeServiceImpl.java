package com.zhang.dga.governance.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.zhang.dga.governance.bean.GovernanceType;
import com.zhang.dga.governance.mapper.GovernanceTypeMapper;
import com.zhang.dga.governance.service.GovernanceTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.springframework.stereotype.Service;


/**
 * <p>
 * 治理考评类别权重表 服务实现类
 * </p>
 *
 * @author zhangyf
 * @since 2023-11-14
 */
@Service
@DS("dga")
public class GovernanceTypeServiceImpl extends ServiceImpl<GovernanceTypeMapper, GovernanceType> implements GovernanceTypeService {

}
