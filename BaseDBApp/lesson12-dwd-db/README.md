# Lesson12 DWD Business Dynamic Split

## 功能目标

本任务用于完成业务数据 DWD 层动态分流流程。

数据链路：

```text
MySQL linda-mall
  -> Maxwell
  -> Kafka topic_ods_base_db
  -> Flink BaseDBApp
  -> 维度数据写入 HBase/Phoenix
  -> 事实数据写入 Kafka DWD 主题


