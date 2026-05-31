# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

新兴云平台（新兴铸管）学习项目。基于 BladeX 商业框架二次开发，内核是 Spring Boot 3.2 + Spring Cloud 2023 + Spring Cloud Alibaba。本项目是入职前四周学习计划的配套代码。

目标公司技术栈见 `docs/` 目录下的技术手册分析。

## 学习路线（四周）

| 周次 | 主题 | 涉及模块 |
|------|------|---------|
| 第一周 | 微服务底座 | cloud-gateway, service-user, service-order |
| 第二周 | 认证鉴权 + 数据层 | cloud-auth, service-user |
| 第三周 | 分布式事务 + 任务调度 | service-order (Seata), service-user (PowerJob) |
| 第四周 | 运维监控 | Prometheus, ELK, SkyWalking |

## 模块架构

```
                    ┌──────────────┐
                    │  Nacos       │  ← 注册中心 + 配置中心
                    └──────┬───────┘
                           │
┌─────────┐     ┌──────────▼──────────┐
│ 用户端   │────▶│  cloud-gateway:8080  │  ← 统一入口
└─────────┘     └──────────┬──────────┘
                           │
              ┌────────────┼────────────┐
              │            │            │
     ┌────────▼───┐ ┌──────▼──────┐ ┌──▼──────────┐
     │cloud-auth  │ │service-user │ │service-order │
     │   :8081    │ │   :8091     │ │   :8092      │
     └────────────┘ └──────┬──────┘ └──────┬───────┘
                           │               │
                    ┌──────▼──────┐ ┌──────▼───────┐
                    │   MySQL     │ │    Seata     │
                    │ learn_user  │ │   (待配置)    │
                    └─────────────┘ └──────────────┘
```

## 常用命令

```bash
# 启动所有中间件
docker-compose up -d

# 停止中间件
docker-compose down

# 编译整个项目
mvn clean compile -DskipTests

# 打包
mvn clean package -DskipTests

# 启动单个服务 (在子模块目录下)
mvn spring-boot:run

# 启动单个服务 (jar)
java -jar service-user/target/service-user-1.0.0.jar

# 检查 Nacos 注册情况
curl http://127.0.0.1:8848/nacos/v1/ns/service/list?pageNo=1&pageSize=10

# 测试用户服务
curl http://127.0.0.1:8080/user/hello
```

## 服务端口

| 服务 | 端口 | 说明 |
|------|------|------|
| cloud-gateway | 8080 | Gateway 网关 |
| cloud-auth | 8081 | OAuth2 认证 |
| service-user | 8091 | 用户服务 |
| service-order | 8092 | 订单服务 |
| Nacos | 8848 | 注册中心 |
| MySQL | 3306 | 数据库 root/root |
| Redis | 6379 | 缓存 redis123 |
| Minio | 9000/9001 | 对象存储 |
| Seata | 8091 | 分布式事务 |

## 技术要点

- **服务注册发现**：所有服务启动后自动注册到 Nacos，Gateway 通过 `lb://service-name` 做负载转发
- **配置中心**：各服务 `application.yml` 中的 Nacos config 地址已配置，后续可将数据库连接串等迁移到 Nacos 配置
- **MyBatis-Plus**：user 和 order 模块已引入，配置了逻辑删除字段 `deleted`
- **Seata**：order 模块已引入依赖，默认关闭 (`seata.enabled=false`)，第三周开启
