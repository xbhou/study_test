# 多 Provider 动态路由与聚合 / Multi-provider Routing & Aggregation

## 目标 / Goal

中文：

这个 Lab 是 Phase 5 的综合实验。它不再只验证一个 API，而是把前面多个稳定性概念组合成一个小型报价系统：

- Dynamic Routing
- Parallel Calls
- Per-provider Timeout
- Overall Deadline
- Circuit Breaker
- Failure Isolation
- Partial Results
- Best Quote Selection

English:

This is the integration lab for Phase 5. It combines routing, concurrency, timeouts, deadlines, circuit breaking, partial results, and best-result selection into one small quote system.

## 架构 / Architecture

~~~text
Quote Request
    |
    v
Provider Router
(symbol / scene / enabled / priority)
    |
    v
Selected Providers
 A      B      C
 |      |      |
 +------|------+
        |
 parallel calls
        |
        +--> per-provider timeout
        +--> circuit breaker
        +--> failure isolation
        |
        v
Provider Outcomes
        |
        +--> overall deadline
        +--> cancel unfinished futures
        |
        v
Valid Quotes
        |
        v
Best Quote
~~~

## 路由规则 / Routing Rules

每个 Provider 都有自己的配置：

~~~text
name
enabled
supportedSymbols
supportedScenes
priority
circuitBreaker
~~~

Router 根据 Request：

~~~text
symbol
scene
maxProviders
~~~

筛选并排序。

例如：

~~~text
Request:
symbol = BTC-USDT
scene  = SPOT

Provider-A -> supports BTC-USDT + SPOT
Provider-B -> supports BTC-USDT + SPOT
Provider-C -> supports BTC-USDT + SPOT
Provider-D -> VIP only
Provider-E -> disabled

Route Result:
A, B, C
~~~

English:

The router filters providers by enabled state, symbol support, scene support, and priority, then limits the fan-out to maxProviders.

## 场景 1：并行报价 + 单 Provider Timeout / Parallel Quotes + Provider Timeout

~~~text
Provider-A  80 ms   100.10
Provider-B 120 ms    99.95
Provider-C 500 ms    99.80

provider timeout = 250 ms
overall deadline = 500 ms
~~~

预期：

~~~text
A -> SUCCESS
B -> SUCCESS
C -> TIMEOUT

best -> B / 99.95
~~~

虽然 C 的价格理论上更好，但它没有在 Provider Timeout 内返回，因此不能进入本次有效候选集。

这体现一个重要设计：

> 最优报价不仅是价格最优，还必须在当前请求的时间预算内可用。

English:

Provider C has the best theoretical price but misses its provider timeout, so it is excluded from the valid candidate set.

The best quote must be both economically attractive and available within the request's time budget.

## 场景 2：Dynamic Routing / 动态路由

同一个 symbol，在不同 scene 下可以走不同 Provider：

~~~text
BTC-USDT + SPOT
-> A / B / C

BTC-USDT + VIP
-> D
~~~

真实系统的 Route Dimension 还可能包括：

- site / tenant
- user tier
- region
- currency
- transaction amount
- provider health score
- cost
- quota
- compliance route

English:

Real routing can include tenant, region, user tier, amount, provider health, cost, quota, and compliance constraints.

## 场景 3：Circuit Breaker 隔离故障 Provider / Circuit-open Provider Is Skipped

假设 Provider-A 连续失败两次：

~~~text
request 1 -> A failure
request 2 -> A failure

A circuit -> OPEN
~~~

第三次请求：

~~~text
Router still selects A and B

A -> CIRCUIT_OPEN
     no real provider call

B -> SUCCESS
~~~

聚合请求仍然可以依靠 B 返回结果。

这就是：

> Provider Failure 应该降级候选集，而不是自动拖垮整个 Quote Request。

English:

Once Provider A's circuit is open, future aggregations fail fast for A without invoking it, while healthy providers continue serving the request.

## 场景 4：Overall Deadline 与 Partial Result

~~~text
Provider-A  80 ms
Provider-B 220 ms
Provider-C 700 ms

provider timeout = 1000 ms
overall deadline = 300 ms
~~~

300ms 左右：

~~~text
A -> SUCCESS
B -> SUCCESS
C -> CANCELLED / unfinished

return partial result
best -> B
~~~

这说明：

~~~text
Per-provider Timeout
!=
Overall Request Deadline
~~~

即使每个 Provider 都允许等待 1000ms，用户请求也可能只允许 300ms。

English:

The overall deadline caps the complete aggregation request even when individual provider timeouts are longer.

## Circuit Breaker 如何接入异步调用 / Circuit Breaker with Async Calls

这个 Lab 没有直接把 Circuit Breaker 包在同步 supplier 外层，因为 Provider Timeout 是 CompletableFuture 层产生的。

流程是：

~~~text
breaker.acquirePermission()
        |
        v
async provider call
        |
   orTimeout(...)
        |
        v
completion
  |           |
success      failure / timeout
  |           |
breaker      breaker
onSuccess    onFailure
~~~

因此 Timeout 也能被 Circuit Breaker 视为一次 Provider Failure。

English:

Permission is acquired before the async call, while breaker success/failure is recorded after timeout-aware completion. That allows provider timeouts to contribute to breaker health.

## Outcome Model / 结果模型

每个 Provider 不直接抛到聚合层，而是转换成 Outcome：

~~~text
SUCCESS
FAILED
TIMEOUT
CIRCUIT_OPEN
CANCELLED
~~~

这样 Aggregator 可以清楚区分：

- Provider 本身失败
- Provider 太慢
- Circuit 已经熔断
- Overall Deadline 导致取消

这对 Metrics 和告警非常重要。

## 运行 / Run

需要 / Requires JDK 17+ and Maven.

~~~bash
mvn clean compile
java -cp target/classes dev.xbhou.javalab.routing.App
~~~

## 生产环境还缺什么 / What Is Still Missing for Production

这个 Lab 是教学架构，不是完整生产系统。

真实系统通常还需要：

- Retry + Backoff + Jitter
- Provider Health Score
- Weighted / Cost-aware Routing
- Bulkhead / Concurrency Limit
- Metrics
- Distributed Configuration
- Dynamic Enable / Disable
- Quote Freshness
- Cache
- Tracing
- Per-provider Deadline Budget
- Request Hedging
- Capacity / Quota Management

## 一个更真实的 Provider Score / A More Realistic Provider Score

Router 未来可以从固定 priority 升级成动态 score：

~~~text
score =
    price quality
  + latency score
  + success rate
  + remaining quota
  - cost
  - circuit penalty
~~~

然后选择 Top-N Provider 并行报价。

这比“永远固定调用 A、B、C”更接近真实多 Provider 系统。

## 与前面 Labs 的关系 / Relationship to Earlier Labs

~~~text
CompletableFuture
      |
Thread Pool
      |
Timeout / Deadline
      |
Retry
      |
Circuit Breaker
      |
Rate Limiting
      |
Dynamic Routing
      |
Aggregation
      |
Best Result
~~~

中文：

前面的 Lab 是单点能力；这个 Lab 开始回答：

> 这些能力在一个真实后端请求链路里应该如何组合？

English:

The earlier labs isolate individual mechanisms. This lab starts combining them into one realistic backend request path.

## 生产设计问题 / Production Design Questions

1. Route Dimension 有哪些？ / What routing dimensions exist?
2. 每次最多 Fan-out 几个 Provider？ / What is the max fan-out?
3. Provider Timeout 如何设置？ / How are provider timeouts chosen?
4. Overall Deadline 如何向下游分配？ / How is the overall deadline budget allocated?
5. Circuit Breaker 是每 Provider 独立吗？ / Is breaker state isolated per provider?
6. Timeout 是否计入 Circuit Failure？ / Do timeouts count as circuit failures?
7. Retry 放在 Provider Client 还是 Aggregator？ / Where should retry live?
8. Partial Result 是否允许返回？ / Are partial results acceptable?
9. Best Quote 除价格外还考虑什么？ / What matters besides price?
10. Provider 被熔断时是否需要备用 Route？ / Is fallback routing needed?
11. 如何避免 Fan-out 放大下游压力？ / How is fan-out amplification controlled?
12. Metrics 如何按 Provider 维度拆分？ / Which per-provider metrics are required?
