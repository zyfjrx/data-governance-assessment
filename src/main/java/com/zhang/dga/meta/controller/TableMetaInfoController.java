package com.zhang.dga.meta.controller;

import com.zhang.dga.meta.bean.TableMetaInfoForQuery;
import com.zhang.dga.meta.bean.vo.TableMetaInfoVO;
import com.zhang.dga.meta.service.TableMetaInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 元数据表 前端控制器
 * </p>
 *
 * @author zhangyf
 * @since 2023-11-08
 */
@RestController
@RequestMapping("/tableMetaInfo")
public class TableMetaInfoController {

    @Autowired
    TableMetaInfoService tableMetaInfoService;

    @GetMapping("/table-list")
    @CrossOrigin
    public Map tableList(TableMetaInfoForQuery tableMetaInfoForQuery){

        //查询列表
        List<TableMetaInfoVO> tableMetaInfoList = tableMetaInfoService.getTableMetaInfoList(tableMetaInfoForQuery);
        //查询总数
        tableMetaInfoForQuery.setPageSize(Integer.MAX_VALUE);
        Integer count = tableMetaInfoService.getTableMetaInfoCount(tableMetaInfoForQuery);
        //封装结果
        Map resultMap=new HashMap();
        resultMap.put("list",tableMetaInfoList);
        resultMap.put("total",count);
        return resultMap;
    }
}
