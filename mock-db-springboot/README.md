# mock-db-springboot

这是一个重新实现的 Linda Mall 业务数据模拟程序。

它只负责向 MySQL 的 `linda-mall` 业务库写数据，不请求 Nginx，也不发送 `/applog`。
写入 MySQL 后，由 Maxwell 监听 binlog 并发送到 Kafka。

## 1. 修改配置

编辑：

```bash
src/main/resources/application.properties
```

把 MySQL 密码改成你自己的：

```properties
spring.datasource.url=jdbc:mysql://192.168.10.101:3306/linda-mall?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=你的MySQL密码
```

## 2. 确认业务库已导入

```sql
USE `linda-mall`;
SHOW TABLES;
```

至少应存在：

- base_trademark
- base_dic
- favor_info
- cart_info
- comment_info
- order_info
- order_detail
- order_status_log
- payment_info

## 3. 启动项目

IDEA 直接运行：

```text
org.example.mockdb.MockDbApplication
```

或命令行：

```bash
mvn clean package -DskipTests
java -jar target/mock-db-springboot-1.0-SNAPSHOT.jar
```

访问页面：

```text
http://localhost:2020/
```

## 4. 常用接口

```bash
# 健康检查
curl http://localhost:2020/api/mock/health

# 查看 MySQL 里所有表
curl http://localhost:2020/api/mock/tables

# 生成维度数据：品牌
curl -X POST "http://localhost:2020/api/mock/trademark?count=1"

# 生成维度数据：字典
curl -X POST "http://localhost:2020/api/mock/base-dic?count=1"

# 生成事实数据：收藏
curl -X POST "http://localhost:2020/api/mock/favor?count=1"

# 生成订单、订单明细、订单状态、支付
curl -X POST "http://localhost:2020/api/mock/order?count=1"

# 一次生成核心表数据
curl -X POST "http://localhost:2020/api/mock/core-all?count=1"

# 通用造数：任意表，根据表结构自动生成字段
curl -X POST "http://localhost:2020/api/mock/table/activity_info?count=1"

# 后台连续生成
curl -X POST "http://localhost:2020/api/mock/run?rounds=100&sleepMs=200"

# 停止后台生成
curl -X POST "http://localhost:2020/api/mock/stop"

# 查看后台日志
curl http://localhost:2020/api/mock/logs
```

## 5. 和实时链路怎么配合

启动顺序：

```text
MySQL
→ ZooKeeper
→ Kafka
→ Maxwell
→ HBase/Phoenix
→ BaseDBApp
→ mock-db-springboot
```

然后点击页面按钮或调用接口造数。

验证 Maxwell 是否采到数据：

```bash
/opt/module/kafka-3.1.0/bin/kafka-console-consumer.sh \
--bootstrap-server hadoop101:9092 \
--topic topic_ods_base_db
```

如果出现包含 `database`、`table`、`type`、`data` 的 JSON，说明：

```text
mock-db-springboot → MySQL → Maxwell → Kafka 成功
```

## 6. BaseDBApp 分流验证

- `base_trademark`、`base_dic`：用于测试维度数据进入 HBase/Phoenix。
- `favor_info`、`cart_info`、`comment_info`、`order_info`、`order_detail`、`payment_info`：用于测试事实数据进入 Kafka DWD topic。

如果规则表 `linda-mall-rt.table_process` 里没有某张表的规则，BaseDBApp 会忽略它。
