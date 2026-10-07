# 开源源码学习路线 / Open Source Learning Path

> 目标 / Goal: 通过真实开源项目，把 Java 后端能力逐步升级为架构型工程能力。  
> Use real open-source projects to grow from a Java backend engineer into an architecture-oriented engineer.
>
> 原则 / Principle: **工作问题 → 开源实现 → 设计推理 → 最小实验 → 学习笔记 → 回到实际系统验证**  
> **work problem → open-source implementation → design reasoning → small experiment → notes → apply back to work**

## 如何使用这份路线 / How to use this roadmap

不要一次性 Clone 或 Fork 所有项目。  
Do **not** clone or fork everything at once.

- **Star**：值得长期关注的项目。 / Projects worth following.
- **Fork**：只有准备做注释、实验或修改时再 Fork。 / Fork only when you are ready to annotate, experiment, or make changes.
- **Clone**：只 Clone 当前正在学习的项目。 / Clone only the project you are actively studying.

每个项目都回答 6 个问题 / For every project, answer six questions:

1. 它解决什么问题？ / What problem does it solve?
2. 主执行链路是什么？ / What is the main execution path?
3. 核心抽象是什么？ / What are the key abstractions?
4. 它如何处理失败场景？ / What failure cases does it handle?
5. 维护者做了哪些权衡？ / What trade-offs did the maintainers make?
6. 哪些设计可以复用到自己的系统？ / What can be reused in our own system design?

---

## 阶段 1 — 当前工作高收益项目 / Stage 1 — High ROI for current backend work

### 1. Caffeine

仓库 / Repository: https://github.com/ben-manes/caffeine

**为什么先学 / Why first:** 项目相对聚焦，但包含高质量的 Java 并发、缓存与性能设计。  
Relatively focused codebase, but full of high-quality Java concurrency and cache design.

学习重点 / Study:
- [ ] `maximumSize`
- [ ] `expireAfterWrite`
- [ ] `refreshAfterWrite`
- [ ] 并发 get / 加载行为 / concurrent get / loading behavior
- [ ] 淘汰算法 / eviction algorithm
- [ ] 异步刷新 / asynchronous refresh

关键问题 / Questions:
- Caffeine 如何避免并发重复加载？ / How does Caffeine avoid repeated loading?
- 过期判断如何做到高效？ / How is expiration checked efficiently?
- 为什么它比简单的 ConcurrentHashMap 缓存更适合生产环境？ / Why is it better suited than a simple ConcurrentHashMap cache?
- 哪些设计可以用于“本地缓存 + Redis”？ / What can be reused in a local-cache + Redis architecture?

### 2. Resilience4j

仓库 / Repository: https://github.com/resilience4j/resilience4j

学习重点 / Study:
- [ ] CircuitBreaker / 熔断
- [ ] Retry / 重试
- [ ] RateLimiter / 限流
- [ ] Bulkhead / 隔离
- [ ] TimeLimiter / 超时控制

用于多提供方 / 多 LP 场景 / Apply to multi-provider / multi-LP calls:

```text
Provider A ─┐
Provider B ─┼─ parallel calls → timeout filtering → valid results → best result
Provider C ─┘
```

关键问题 / Questions:
- 某个提供方从 50ms 变成 3s 会发生什么？ / What happens when one provider changes from 50 ms to 3 s?
- Timeout、Retry、CircuitBreaker、Bulkhead 应该放在哪里？ / Where should timeout, retry, circuit breaker and isolation sit?
- 全局 Deadline Budget 应该如何分配？ / How should a global deadline budget be allocated?

### 3. Nacos

仓库 / Repository: https://github.com/alibaba/nacos

学习重点 / Study:
- [ ] 服务注册 / service registration
- [ ] 服务发现 / service discovery
- [ ] 健康检查 / health checking
- [ ] 动态配置 / dynamic configuration
- [ ] 客户端与服务端更新机制 / client/server update mechanism

关键问题 / Questions:
- 实例信息保存在哪里？ / Where is instance information stored?
- 客户端如何感知服务变化？ / How does the client discover changes?
- 配置变更如何传播？ / How does configuration change propagation work?
- 服务端或网络异常时发生什么？ / What happens during server/network failure?

### 4. gRPC Java

仓库 / Repository: https://github.com/grpc/grpc-java

学习重点 / Study:
- [ ] ManagedChannel
- [ ] ClientCall
- [ ] NameResolver
- [ ] LoadBalancer
- [ ] Deadline
- [ ] HTTP/2 Stream
- [ ] Flow Control
- [ ] Retry

重点理解的线上错误 / Production errors to understand:
- DEADLINE_EXCEEDED
- RESOURCE_EXHAUSTED
- UNAVAILABLE

---

## 阶段 2 — 分布式后端基础 / Stage 2 — Distributed backend fundamentals

### 5. Apache Kafka

仓库 / Repository: https://github.com/apache/kafka

学习重点 / Study:
- [ ] Producer
- [ ] Partition
- [ ] Broker
- [ ] Replica
- [ ] Consumer Group
- [ ] Offset
- [ ] Rebalance
- [ ] Consumer Lag

关键问题 / Key questions:
- 为什么 Lag 会持续增长？ / Why does lag keep growing?
- 增加 Consumer 一定有效吗？ / Does adding consumers always help?
- Partition 与 Consumer 是什么关系？ / What is the relationship between partitions and consumers?
- Rebalance 为什么影响吞吐？ / Why does rebalance affect throughput?
- 瓶颈到底在 Kafka 还是业务代码？ / Is the bottleneck Kafka or application code?

### 6. Apache ShardingSphere

仓库 / Repository: https://github.com/apache/shardingsphere

重点理解执行链路 / Study the execution pipeline:

```text
SQL
 ↓
Parser
 ↓
Binder
 ↓
Router
 ↓
Rewriter
 ↓
Executor
 ↓
Merge
```

目标 / Goal: 理解中间件如何把一条 SQL 转换成分布式执行计划。  
Understand how middleware transforms a database request into a distributed execution plan.

### 7. Redisson

仓库 / Repository: https://github.com/redisson/redisson

学习重点 / Study:
- [ ] 分布式锁 / distributed lock
- [ ] Watchdog / Lease Renewal
- [ ] 同步原语 / synchronization primitives
- [ ] 基于 Redis 的 Java 抽象 / Redis-based Java abstractions

### 8. Apache Dubbo

仓库 / Repository: https://github.com/apache/dubbo

学习重点 / Study:
- [ ] RPC 抽象 / RPC abstraction
- [ ] 服务注册 / service registration
- [ ] 负载均衡 / load balancing
- [ ] SPI / 扩展机制 / extension mechanism

### 9. Apache RocketMQ

仓库 / Repository: https://github.com/apache/rocketmq

学习重点 / Study:
- [ ] 消息可靠性 / message reliability
- [ ] Broker
- [ ] 顺序消息 / ordered messages
- [ ] 事务消息 / transactional messages

### 10. Apache Seata

仓库 / Repository: https://github.com/apache/incubator-seata

学习重点 / Study:
- [ ] AT
- [ ] TCC
- [ ] Saga
- [ ] XA

---

## 阶段 3 — 框架内部原理 / Stage 3 — Framework internals

### 11. Spring Boot

仓库 / Repository: https://github.com/spring-projects/spring-boot

学习重点 / Study:
- [ ] 自动配置 / auto-configuration
- [ ] Starter 机制 / starter mechanism
- [ ] 应用启动生命周期 / application startup lifecycle
- [ ] Actuator

### 12. Spring Framework

仓库 / Repository: https://github.com/spring-projects/spring-framework

建议在较小项目之后再系统阅读。  
Do this **after** smaller projects.

推荐顺序 / Suggested order:

```text
ApplicationContext
  ↓
BeanFactory
  ↓
BeanDefinition
  ↓
BeanFactoryPostProcessor
  ↓
BeanPostProcessor
  ↓
Bean lifecycle
  ↓
AOP
  ↓
Transaction
```

### 13. Spring Security

仓库 / Repository: https://github.com/spring-projects/spring-security

学习重点 / Study:
- [ ] FilterChain
- [ ] Authentication / 认证
- [ ] Authorization / 授权
- [ ] OAuth2

### 14. Google Guava

仓库 / Repository: https://github.com/google/guava

学习重点 / Study:
- [ ] 集合 API / collection APIs
- [ ] 工具类设计 / utilities
- [ ] 缓存相关抽象 / cache-related abstractions
- [ ] API 设计 / API design

---

## 阶段 4 — 架构与基础设施深度 / Stage 4 — Architecture and infrastructure depth

### 15. Netty

仓库 / Repository: https://github.com/netty/netty

重点链路 / Study:

```text
Socket
 ↓
Selector
 ↓
EventLoop
 ↓
Channel
 ↓
Pipeline
 ↓
Handler
```

核心抽象 / Key abstractions:
- [ ] Channel
- [ ] Pipeline
- [ ] Handler
- [ ] EventLoop
- [ ] ByteBuf
- [ ] Future / Promise

### 16. Apache SkyWalking

仓库 / Repository: https://github.com/apache/skywalking

学习重点 / Study:
- [ ] 链路追踪 / tracing
- [ ] Java Agent
- [ ] Context Propagation / 上下文传播
- [ ] 可观测性架构 / observability architecture

### 17. OpenTelemetry Java

仓库 / Repository: https://github.com/open-telemetry/opentelemetry-java

学习重点 / Study:
- [ ] Trace
- [ ] Metric
- [ ] Context
- [ ] Propagation
- [ ] Instrumentation

### 18. Elasticsearch

仓库 / Repository: https://github.com/elastic/elasticsearch

学习重点 / Study:
- [ ] Shard
- [ ] Replica
- [ ] 分布式搜索 / distributed search
- [ ] 集群设计 / cluster design

### 19. Apache Flink

仓库 / Repository: https://github.com/apache/flink

学习重点 / Study:
- [ ] 流式计算 / stream processing
- [ ] 状态 / state
- [ ] Checkpoint
- [ ] 容错 / fault tolerance

### 20. Alibaba Sentinel

仓库 / Repository: https://github.com/alibaba/Sentinel

学习重点 / Study:
- [ ] 流控 / flow control
- [ ] 限流 / rate limiting
- [ ] 熔断 / circuit breaking
- [ ] 系统保护规则 / system protection rules

---

## 推荐优先级 / Recommended priority

不要把 20 个仓库视为同等优先级。  
Do **not** treat all 20 repositories equally.

### A — 现在开始 / Start here

1. [ ] Caffeine
2. [ ] Resilience4j
3. [ ] Nacos
4. [ ] gRPC Java

### B — 分布式系统基础 / Distributed system foundations

5. [ ] Kafka
6. [ ] ShardingSphere
7. [ ] Redisson
8. [ ] Dubbo

### C — 框架内部 / Framework internals

9. [ ] Spring Boot
10. [ ] Spring Framework
11. [ ] Netty

### D — 架构扩展 / Architecture expansion

12. [ ] SkyWalking
13. [ ] OpenTelemetry Java
14. [ ] RocketMQ
15. [ ] Seata
16. [ ] Spring Security
17. [ ] Guava
18. [ ] Elasticsearch
19. [ ] Flink
20. [ ] Sentinel

---

## 推荐学习循环 / Suggested learning loop

每个仓库都使用同一套流程 / Use the same loop for each repository:

```text
1. 选择一个具体问题 / Pick one concrete question
        ↓
2. 阅读 README / 架构文档 / Read README / architecture docs
        ↓
3. 找到主入口 / Find the main entry point
        ↓
4. 跟踪一条完整执行链路 / Trace one execution path
        ↓
5. 构建最小可运行实验 / Build the smallest runnable experiment
        ↓
6. 观察行为与失败模式 / Observe behavior / failure mode
        ↓
7. 写一篇简短学习笔记 / Write a short note
        ↓
8. 映射回真实生产架构 / Relate it back to a real production design
```

避免 / Avoid:

```text
clone project
→ 打开几千个文件 / open thousands of files
→ 随机阅读 / read randomly
→ 理解很少 / understand very little
→ 放弃 / abandon project
```

---

## 第一个具体任务 — Caffeine / First concrete task — Caffeine

从一个问题开始 / Start with one question:

> Caffeine 如何在高并发下实现高性能本地缓存？  
> How does Caffeine implement high-performance local caching under concurrency?

初始清单 / Initial checklist:

- [ ] Star 仓库 / Star the repository
- [ ] 阅读 README / Read README
- [ ] 运行一个基础缓存示例 / Run one basic cache example
- [ ] 跟踪 `Caffeine.newBuilder()` / Trace `Caffeine.newBuilder()`
- [ ] 跟踪 `build()` / Trace `build()`
- [ ] 理解 `maximumSize` / Understand `maximumSize`
- [ ] 理解过期机制 / Understand expiration
- [ ] 理解刷新机制 / Understand refresh
- [ ] 编写 `docs/source-reading/caffeine-01.md` / Write `docs/source-reading/caffeine-01.md`
- [ ] 与简单 ConcurrentHashMap 缓存比较 / Compare with a simple ConcurrentHashMap cache

---

## 长期目标 / Long-term target

目标不是 / The goal is not:

> “我读过 20 个开源项目。”  
> “I have read 20 open-source projects.”

真正目标是 / The real goal is:

> “遇到生产问题时，我能够找到成熟开源实现，理解它的设计选择，用实验验证，并把它转化为自己的架构能力。”  
> “When I encounter a production problem, I can find a mature implementation, understand its design choices, validate them with experiments, and turn them into my own architecture capability.”
