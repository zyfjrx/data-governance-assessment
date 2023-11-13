package com.zhang.dga.meta.bean;

import lombok.Data;

/**
 * @title:
 * @author: zhangyf
 * @date: 2023/11/11 8:13
 **/
@Data
public class TableMetaInfoForQuery {

    private Integer pageNo=1;

    private Integer pageSize=20;

    /**
     * 表名
     */
    private String tableName;

    /**
     * 库名
     */
    private String schemaName;

    /**
     * 数仓所在层级(ODSDWDDIMDWSADS) ( 来源: 附加)
     */
    private String dwLevel;
}
