# 熔断器实验 / Circuit Breaker Lab

## 问题 / Question

中文：

当一个下游服务持续失败或严重变慢时，如果调用方继续不断请求，常见结果是：

- 更多线程阻塞
- Connection Pool 被占满
- Retry 放大流量
- 上游延迟持续升高
- 故障从一个依赖扩散到整个系统

Circuit Breaker 的目标是：

> 当依赖明显不健康时，暂时停止真实调用，让失败快速返回；经过一段时间后，再用少量探测请求判断是否恢复。

English:

When a downstream dependency keeps failing or slowing down, blindly continuing calls can consume threads, exhaust connection pools, amplify retries, and spread the failure upstream.

A Circuit Breaker temporarily stops real calls, fails fast, and later allows limited probe traffic to test recovery.

## 状态机 / State Machine

~~~text
              failures reach threshold
        +------------------------------+
        |                              v
     CLOSED ------------------------> OPEN
       ^                                |
       |                                | open duration elapsed
       |                                v
       +-------------------------- HALF_OPEN
          probe succeeds              |
                                      |
                                      | probe fails
                                      +--------> OPEN
~~~

### CLOSED

中文：

正常状态。请求会真正调用下游。

这个教学实现使用：

~~~text
consecutiveFailureThreshold = 3
~~~

连续失败达到阈值后进入 OPEN。

English:

Normal state. Calls reach the downstream dependency. This teaching implementation opens after a configured number of consecutive failures.

### OPEN

中文：

不再调用真实下游，而是立即 Fail Fast。

~~~text
request
  |
  v
Circuit Breaker OPEN
  |
  +--> reject immediately
~~~

这可以减少：

- 无意义下游流量
- Thread / Connection 占用
- Timeout 等待
- Retry Storm

English:

Calls are rejected immediately without reaching the dependency. This reduces wasted downstream traffic, resource occupation, and timeout waiting.

### HALF_OPEN

中文：

OPEN 持续一段时间后，不能直接假设下游已经恢复。

因此进入 HALF_OPEN，只允许少量 Probe Request。

本实验一次只允许一个 Probe。

~~~text
OPEN
  |
wait 1000 ms
  |
  v
HALF_OPEN
  |
one probe
 /      \
success  failure
 |        |
CLOSED   OPEN
~~~

English:

After the open duration expires, the breaker does not immediately trust the dependency again. It enters HALF_OPEN and allows limited probe traffic.

This demo allows one probe at a time.

## 场景 1：连续失败触发 OPEN / Consecutive Failures Open the Circuit

配置：

~~~text
failure threshold = 3
open duration     = 1000 ms
~~~

前三次下游调用失败：

~~~text
call 1 -> failure
call 2 -> failure
call 3 -> failure

state -> OPEN
~~~

第 4 次请求：

~~~text
CircuitOpenException
downstream invocation count does not increase
~~~

关键点：

> OPEN 状态是 Fail Fast，不是“继续调用下游再快速失败”。

## 场景 2：OPEN 到期后进入 HALF_OPEN / OPEN to HALF_OPEN

推进时间：

~~~text
+1000 ms
~~~

下一次请求成为 Probe。

如果 Probe 成功：

~~~text
HALF_OPEN
   |
success
   v
CLOSED
~~~

之后普通请求重新通过。

## 场景 3：HALF_OPEN Probe 失败 / Failed Probe Reopens

如果 Probe 失败：

~~~text
HALF_OPEN
   |
failure
   v
OPEN
~~~

同时重新开始 OPEN 等待时间。

这避免依赖刚有一点恢复迹象就被大量流量重新压垮。

## 为什么用 ManualClock / Why a Manual Clock

为了让实验完全可重复，本 Lab 不使用真实 sleep 等待 1 秒，而是手动推进时钟。

~~~text
clock.advanceMillis(1000)
~~~

这样可以稳定验证 OPEN Duration，而不会受机器调度影响。

## 运行 / Run

需要 / Requires JDK 17+ and Maven.

~~~bash
mvn clean compile
java -cp target/classes dev.xbhou.javalab.circuitbreaker.App
~~~

## 教学模型 vs 生产实现 / Teaching Model vs Production Implementation

本 Lab 使用“连续失败次数”是为了把状态机讲清楚。

生产系统通常还会考虑：

- Sliding Window
- Failure Rate
- Slow Call Rate
- Minimum Number of Calls
- HALF_OPEN Permitted Calls
- Exception Classification
- Call Timeout
- Metrics
- Event Listener

例如一个生产策略可能是：

~~~text
last 100 calls
failure rate >= 50%
minimum calls >= 20
=> OPEN
~~~

而不是简单的：

~~~text
3 failures => OPEN
~~~

English:

This lab uses consecutive failures to make state transitions obvious. Production implementations often use sliding windows, failure rates, slow-call rates, minimum call counts, and configurable half-open probe limits.

## 哪些异常应该计入失败 / Failure Classification

不是所有异常都应该让 Circuit Breaker 记一次 Failure。

例如：

可能计入：

- Connection Timeout
- HTTP 502 / 503 / 504
- gRPC UNAVAILABLE
- Downstream Timeout

通常不应计入依赖健康度：

- 用户参数错误
- 权限不足
- Insufficient Balance
- 明确业务拒绝

否则用户输入错误也可能把下游服务“熔断”。

## Circuit Breaker 与 Retry / Circuit Breaker vs Retry

~~~text
Retry
-> 这次失败后，要不要再试一次？

Circuit Breaker
-> 这个依赖目前还值得继续调用吗？
~~~

组合不当时：

~~~text
dependency failing
      |
      v
retry retry retry
      |
      v
more load on unhealthy dependency
~~~

Circuit Breaker 可以帮助 Retry 更快停止无意义尝试。

但 Retry 仍然需要：

- Backoff
- Jitter
- Deadline Budget

## Circuit Breaker 与 Rate Limiting / Circuit Breaker vs Rate Limiting

~~~text
Rate Limiting
-> incoming traffic exceeds planned capacity

Circuit Breaker
-> downstream dependency is unhealthy
~~~

一个保护“进入系统的流量”，另一个保护“对故障依赖的调用”。

## Circuit Breaker 与 Bulkhead

Circuit Breaker 并不能限制同时有多少调用正在占用资源。

即使熔断阈值还没达到，1000 个慢请求也可能先耗尽线程。

因此真实稳定性设计常把它和 Bulkhead / Concurrency Limit 一起使用：

~~~text
Timeout
Retry
Circuit Breaker
Bulkhead
Rate Limiting
Deadline
~~~

每个机制解决不同问题。

## 生产设计问题 / Production Design Questions

1. 什么错误计入 Failure？ / Which failures count?
2. 使用连续失败还是 Failure Rate？ / Consecutive failures or failure rate?
3. Sliding Window 多大？ / How large is the sliding window?
4. 最少多少请求才允许熔断？ / What is the minimum call count?
5. OPEN 多久？ / How long should OPEN last?
6. HALF_OPEN 允许多少 Probe？ / How many half-open probes?
7. Slow Call 是否计入？ / Do slow calls count?
8. Probe 成功几次才恢复？ / How many successful probes are needed?
9. Fallback 是什么？ / What is the fallback?
10. Circuit State 如何监控？ / How is circuit state observed?
11. 多实例之间是否共享状态？ / Is state local or shared across instances?
12. Retry 是否在 Circuit Breaker 内外正确编排？ / How is retry composed with the breaker?

## 下一步 / Next Step

完成 Circuit Breaker 后，Phase 5 剩下的重要主题是：

> Multi-provider Routing & Aggregation

它会把前面的 Timeout、Deadline、Retry、Circuit Breaker、Rate Limiting 和并行聚合逐渐组合成一个更接近真实后端架构的实验。
