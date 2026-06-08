# 企业级分布式实时数仓分析平台

> 海康 2026 实习实训项目 | 基于 Flink + Kafka + HBase + ClickHouse 的完整实时数仓链路

## 项目概述

本项目以企业真实业务场景为背景，构建一套完整的**企业级分布式实时数仓分析平台**，覆盖从数据采集到实时大屏展示的端到端数据处理链路。

```
实时数据采集 → Kafka 传输 → Flink 实时计算 → 分层数仓（ODS/DWD/DIM/DWS）→ ClickHouse/HBase 存储 → Sugar BI 大屏展示
```

## 技术栈

| 类别 | 技术 |
|------|------|
| 实时计算 | Apache Flink |
| 消息队列 | Apache Kafka |
| 列式存储 | ClickHouse (OLAP) |
| 分布式数据库 | HBase |
| 数据采集 | Maxwell (MySQL Binlog) |
| 日志服务 | Spring Boot + Log4j |
| 负载均衡 | Nginx |
| 资源调度 | YARN + ZooKeeper |
| 数据可视化 | Sugar BI |
| 后端服务 | Spring Boot (SSM) + Swagger |
| 工程协作 | Git + GitHub |
| AI 辅助开发 | Claude Code / Trae / Cursor |

## 数仓分层架构

```
┌─────────────────────────────────────────────────────────────┐
│                        App 层（应用层）                        │
│              Sugar BI 实时运营大屏 / SSM 接口服务               │
├─────────────────────────────────────────────────────────────┤
│                        DWS 层（数据服务层）                     │
│       实时 GMV、UV/PV、转化率、跳出率、热门商品排行、省市热力       │
├─────────────────────────────────────────────────────────────┤
│              DWD 层（数据明细层）       DIM 层（维度层）          │
│        业务明细数据、行为分析数据            维度数据关联           │
├─────────────────────────────────────────────────────────────┤
│                        ODS 层（原始数据层）                     │
│          Binlog / 用户行为日志 / 系统日志 / 埋点日志              │
└─────────────────────────────────────────────────────────────┘
```

## 项目结构

```
DSP/
├── docs/                    # 项目文档
│   ├── 项目方案.pdf
│   └── 技术文档/
├── dsp-logger/              # Spring Boot 日志生成模块（ODS 数据源）
├── dsp-flink/               # Flink 实时处理核心模块
│   ├── ods/                 # ODS 层数据处理
│   ├── dwd/                 # DWD 层数据清洗与分流
│   ├── dim/                 # DIM 维度数据处理
│   └── dws/                 # DWS 层指标聚合
├── dsp-service/             # SSM 后端接口服务（连接 ClickHouse/HBase）
├── dsp-common/              # 公共工具类与常量
├── scripts/                 # 集群部署与启动脚本
│   ├── kafka/
│   ├── zookeeper/
│   ├── hbase/
│   └── clickhouse/
└── README.md
```

## 实训安排（15 天）

| 天数 | 主题 |
|------|------|
| Day 1-5 | Flink 系统搭建、DataStream API、时间窗口、状态与分流 |
| Day 6 | Git 代码托管与企业级分支管理 |
| Day 7 | Spring Boot 框架应用 |
| Day 8-10 | 实时数仓环境搭建、ZK/Kafka 集群、ODS 层、日志采集 |
| Day 11-12 | Maxwell 业务数据处理、HBase 环境搭建 |
| Day 13 | 数据分析计算（宽表、双流 JOIN、UV/PV 算法） |
| Day 14 | AI 辅助开发（Claude Code、SSM 架构生成） |
| Day 15 | 系统联调、Sugar BI 大屏、发布答辩 |

## 最终成果

- ✅ 企业级实时数仓平台（完整数据链路运行）
- ✅ 实时数据分析大屏（Sugar BI）
- ✅ 完整项目源码（符合企业编码规范）
- ✅ GitHub 项目仓库
- ✅ 项目技术文档
- ✅ 项目答辩成果

## 开发规范

- 分支策略：`main`（主分支）/ `dev`（开发分支）/ `feature/xxx`（功能分支）
- 提交规范：`feat:` / `fix:` / `docs:` / `refactor:` / `chore:`
- 每日提交代码至对应功能分支

---

*海康实习实训 · 2026*
