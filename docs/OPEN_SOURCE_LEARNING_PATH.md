# Open Source Learning Path

> Goal: use real open-source projects to grow from a Java backend engineer into an architecture-oriented engineer.
>
> Principle: **work problem → open-source implementation → design reasoning → small experiment → notes → apply back to work**.

## How to use this roadmap

Do **not** clone or fork everything at once.

- **Star**: projects worth following.
- **Fork**: only when you are ready to annotate, experiment, or make changes.
- **Clone**: only the project you are actively studying.
- For every project, write down:
  1. What problem does it solve?
  2. What is the main execution path?
  3. What are the key abstractions?
  4. What failure cases does it handle?
  5. What trade-offs did the maintainers make?
  6. What can be reused in our own system design?

---

## Stage 1 — High ROI for current backend work

### 1. Caffeine
Repository: https://github.com/ben-manes/caffeine

**Why first:** relatively focused codebase, but full of high-quality Java concurrency and cache design.

Study:
- [ ] maximumSize
- [ ] expireAfterWrite
- [ ] refreshAfterWrite
- [ ] concurrent get / loading behavior
- [ ] eviction algorithm
- [ ] asynchronous refresh

Questions:
- How does Caffeine avoid repeated loading?
- How is expiration checked efficiently?
- Why is it faster than a simple ConcurrentHashMap cache?
- What can be reused in a local-cache + Redis architecture?

### 2. Resilience4j
Repository: https://github.com/resilience4j/resilience4j

Study:
- [ ] CircuitBreaker
- [ ] Retry
- [ ] RateLimiter
- [ ] Bulkhead
- [ ] TimeLimiter

Apply to multi-provider / multi-LP calls:

```text
Provider A ─┐
Provider B ─┼─ parallel calls → timeout filtering → valid results → best result
Provider C ─┘
```

Questions:
- What happens when one provider changes from 50 ms to 3 s?
- Where should timeout, retry, circuit breaker and isolation sit?
- How should a global deadline budget be allocated?

### 3. Nacos
Repository: https://github.com/alibaba/nacos

Study:
- [ ] service registration
- [ ] service discovery
- [ ] health checking
- [ ] dynamic configuration
- [ ] client/server update mechanism

Questions:
- Where is instance information stored?
- How does the client discover changes?
- How does configuration change propagation work?
- What happens during server/network failure?

### 4. gRPC Java
Repository: https://github.com/grpc/grpc-java

Study:
- [ ] ManagedChannel
- [ ] ClientCall
- [ ] NameResolver
- [ ] LoadBalancer
- [ ] Deadline
- [ ] HTTP/2 stream
- [ ] flow control
- [ ] retry

Focus on production errors:
- DEADLINE_EXCEEDED
- RESOURCE_EXHAUSTED
- UNAVAILABLE

---

## Stage 2 — Distributed backend fundamentals

### 5. Apache Kafka
Repository: https://github.com/apache/kafka

Study:
- [ ] Producer
- [ ] Partition
- [ ] Broker
- [ ] Replica
- [ ] Consumer Group
- [ ] Offset
- [ ] Rebalance
- [ ] Consumer Lag

Key questions:
- Why does lag keep growing?
- Does adding consumers always help?
- What is the relationship between partitions and consumers?
- Why does rebalance affect throughput?
- Is the bottleneck Kafka or application code?

### 6. Apache ShardingSphere
Repository: https://github.com/apache/shardingsphere

Study the execution pipeline:

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

Goal: understand how middleware transforms a database request into a distributed execution plan.

### 7. Redisson
Repository: https://github.com/redisson/redisson

Study:
- [ ] distributed lock
- [ ] watchdog / lease renewal
- [ ] synchronization primitives
- [ ] Redis-based Java abstractions

### 8. Apache Dubbo
Repository: https://github.com/apache/dubbo

Study:
- [ ] RPC abstraction
- [ ] service registration
- [ ] load balancing
- [ ] extension/SPI mechanism

### 9. Apache RocketMQ
Repository: https://github.com/apache/rocketmq

Study:
- [ ] message reliability
- [ ] Broker
- [ ] ordered messages
- [ ] transactional messages

### 10. Apache Seata
Repository: https://github.com/apache/incubator-seata

Study:
- [ ] AT
- [ ] TCC
- [ ] Saga
- [ ] XA

---

## Stage 3 — Framework internals

### 11. Spring Boot
Repository: https://github.com/spring-projects/spring-boot

Study:
- [ ] auto-configuration
- [ ] starter mechanism
- [ ] application startup lifecycle
- [ ] Actuator

### 12. Spring Framework
Repository: https://github.com/spring-projects/spring-framework

Do this **after** smaller projects.

Suggested order:

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
Repository: https://github.com/spring-projects/spring-security

Study:
- [ ] FilterChain
- [ ] Authentication
- [ ] Authorization
- [ ] OAuth2

### 14. Google Guava
Repository: https://github.com/google/guava

Study:
- [ ] collection APIs
- [ ] utilities
- [ ] cache-related abstractions
- [ ] API design

---

## Stage 4 — Architecture and infrastructure depth

### 15. Netty
Repository: https://github.com/netty/netty

Study:

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

Key abstractions:
- [ ] Channel
- [ ] Pipeline
- [ ] Handler
- [ ] EventLoop
- [ ] ByteBuf
- [ ] Future / Promise

### 16. Apache SkyWalking
Repository: https://github.com/apache/skywalking

Study:
- [ ] tracing
- [ ] Java agent
- [ ] context propagation
- [ ] observability architecture

### 17. OpenTelemetry Java
Repository: https://github.com/open-telemetry/opentelemetry-java

Study:
- [ ] Trace
- [ ] Metric
- [ ] Context
- [ ] propagation
- [ ] instrumentation

### 18. Elasticsearch
Repository: https://github.com/elastic/elasticsearch

Study:
- [ ] shard
- [ ] replica
- [ ] distributed search
- [ ] cluster design

### 19. Apache Flink
Repository: https://github.com/apache/flink

Study:
- [ ] stream processing
- [ ] state
- [ ] checkpoint
- [ ] fault tolerance

### 20. Alibaba Sentinel
Repository: https://github.com/alibaba/Sentinel

Study:
- [ ] flow control
- [ ] rate limiting
- [ ] circuit breaking
- [ ] system protection rules

---

## Recommended priority

Do **not** treat all 20 repositories equally.

### Priority A — Start here
1. [ ] Caffeine
2. [ ] Resilience4j
3. [ ] Nacos
4. [ ] gRPC Java

### Priority B — Distributed system foundations
5. [ ] Kafka
6. [ ] ShardingSphere
7. [ ] Redisson
8. [ ] Dubbo

### Priority C — Framework internals
9. [ ] Spring Boot
10. [ ] Spring Framework
11. [ ] Netty

### Priority D — Architecture expansion
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

## Suggested learning loop

For each repository:

```text
1. Pick one concrete question
        ↓
2. Read README / architecture docs
        ↓
3. Find the main entry point
        ↓
4. Trace one execution path
        ↓
5. Build the smallest runnable experiment
        ↓
6. Observe behavior / failure mode
        ↓
7. Write a short note
        ↓
8. Relate it back to a real production design
```

Avoid:

```text
clone project
→ open thousands of files
→ read randomly
→ understand very little
→ abandon project
```

---

## First concrete task — Caffeine

Start with one question:

> How does Caffeine implement high-performance local caching under concurrency?

Initial checklist:

- [ ] Star the repository
- [ ] Read README
- [ ] Run one basic cache example
- [ ] Trace `Caffeine.newBuilder()`
- [ ] Trace `build()`
- [ ] Understand `maximumSize`
- [ ] Understand expiration
- [ ] Understand refresh
- [ ] Write `docs/source-reading/caffeine-01.md`
- [ ] Compare Caffeine's design with a simple ConcurrentHashMap cache

---

## Long-term target

The goal is not:

> “I have read 20 open-source projects.”

The goal is:

> “When I encounter a production problem, I can find a mature implementation, understand its design choices, validate them with experiments, and turn them into my own architecture capability.”
