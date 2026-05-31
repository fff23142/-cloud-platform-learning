# 新兴云平台 技术手册分析

来源：入职部门发送的 `新兴云平台.xlsx`

## 技术架构

- 基础：SpringBoot3 + SpringCloud 2023 + MyBatis
- 认证：OAuth2 协议统一 Token 下发与鉴权
- 网关：Gateway + 生产环境 Traefik 代理
- 注册配置：Nacos（注册中心 + 配置中心）
- 远程调用：Feign + Ribbon 负载 + Hystrix 熔断
- 限流：Sentinel
- 分布式事务：Seata
- 部署：FatJar / Docker / K8s / 阿里云

## 研发任务分布（微服务版 53项）

### Bladex-Cloud（基础框架）
- 版本：4.1.0 → 4.4.0
- SaaS 多租户、OAuth2 认证、Secure 安全框架（功能/接口/数据权限）
- 事务：Seata 分布式事务、乐观锁、Redis 分布式锁、Spring 事务锁
- 存储：Minio 对象存储
- 调度：PowerJob 分布式任务调度
- 报表：UReport2
- 工作流：NutFlow / AgileBPM / 炎黄（选型中）
- API 报文加密

### Bladex-Biz（业务框架）
- 业务框架模板、动态数据源、读写分离分库分表
- 代码生成器（单表/主子表/树表）
- LiteFlow 规则引擎
- 第三方集成：人力系统、ERP、帆软、eplat 统一认证、ixbus 电文

### 系统服务
- ELK 分布式日志追踪
- SkyWalking 链路追踪
- Nacos / Sentinel / Prometheus + Grafana
- GitLab / Nexus / Nginx / Redis / Minio / Seata 管理
- Supervisord 进程守护、Jenkins DevOps
- kkFileView 附件预览

## 其他模块
- 单体版：BladeX-Boot，部署 + 基础组件
- 物联网：PLC → OPC-UA → MQTT，TDengine 时序数据库
- APP：Android / iOS / 鸿蒙打包
- 大屏：独立前后端部署
