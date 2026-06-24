CREATE DATABASE IF NOT EXISTS `linda-mall-rt` DEFAULT CHARSET utf8;

USE `linda-mall-rt`;

DROP TABLE IF EXISTS `table_process`;

CREATE TABLE `table_process` (
  `source_table` varchar(200) NOT NULL COMMENT '来源表',
  `operate_type` varchar(200) NOT NULL COMMENT '操作类型 insert,update,delete',
  `sink_type` varchar(200) DEFAULT NULL COMMENT '输出类型 hbase/kafka',
  `sink_table` varchar(200) DEFAULT NULL COMMENT '输出表或主题',
  `sink_columns` varchar(2000) DEFAULT NULL COMMENT '输出字段',
  `sink_pk` varchar(200) DEFAULT NULL COMMENT '主键字段',
  `sink_extend` varchar(200) DEFAULT NULL COMMENT '建表扩展',
  PRIMARY KEY (`source_table`,`operate_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `table_process`
(source_table, operate_type, sink_type, sink_table, sink_columns, sink_pk, sink_extend)
VALUES
('base_trademark', 'insert', 'hbase', 'DIM_BASE_TRADEMARK', 'id,tm_name,logo_url', 'id', null),
('base_trademark', 'update', 'hbase', 'DIM_BASE_TRADEMARK', 'id,tm_name,logo_url', 'id', null),
('base_dic', 'insert', 'hbase', 'DIM_BASE_DIC', 'id,dic_name,parent_code,create_time', 'id', null),
('base_dic', 'update', 'hbase', 'DIM_BASE_DIC', 'id,dic_name,parent_code,create_time', 'id', null),
('favor_info', 'insert', 'kafka', 'dwd_favor_info', 'id,user_id,sku_id,spu_id,is_cancel,create_time,cancel_time', 'id', null),
('order_info', 'insert', 'kafka', 'dwd_order_info', 'id,user_id,province_id,order_status,total_amount,create_time,operate_time,final_total_amount', 'id', null),
('order_info', 'update', 'kafka', 'dwd_order_info', 'id,user_id,province_id,order_status,total_amount,create_time,operate_time,final_total_amount', 'id', null);
