package org.example.rulemanager.entity;

import lombok.Data;

@Data
public class TableProcess {

    /**
     * 来源表，例如 base_trademark
     */
    private String sourceTable;

    /**
     * 操作类型 insert/update/delete
     */
    private String operateType;

    /**
     * 输出类型 hbase/kafka
     */
    private String sinkType;

    /**
     * 输出表或 Kafka 主题
     */
    private String sinkTable;

    /**
     * 输出字段，例如 id,tm_name
     */
    private String sinkColumns;

    /**
     * 主键字段，例如 id
     */
    private String sinkPk;

    /**
     * 建表扩展，可以为空
     */
    private String sinkExtend;
}