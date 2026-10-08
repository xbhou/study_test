# 限流实验 / Rate Limiting Lab

## 问题 / Question

中文：

限流不是简单地“每秒最多 N 个请求”。不同算法在突发流量、窗口边界、公平性和实现成本上有明显差异。

这个 Lab 对比三种常见算法：

- Fixed Window
- Sliding Window Log
- Token Bucket

English:

Rate limiting is more than “allow at most N requests per second”. Different algorithms behave differently around bursts, window boundaries, fairness, and implementation cost.

This lab compares:

- Fixed Window
- Sliding Window Log
- Token Bucket

## 为什么使用 ManualClock / Why a Manual Clock

中文：

为了让实验结果可重复，本 Lab 不依赖真实时间和 Thread.sleep，而是通过 ManualClock 主动推进时间。

这样可以精确复现“999ms 和 1001ms”这种窗口边界问题。

English:

The lab uses a ManualClock instead of wall-clock time or Thread.sleep. This makes boundary behavior deterministic and reproducible.

## 场景 1：Fixed Window 边界突发 / Fixed Window Boundary Burst

配置：

~~~text
limit = 5 requests / 1000 ms
~~~

先在窗口结尾发送 5 个请求：

~~~text
t = 990 ms
5 requests -> all allowed
~~~

然后跨过窗口边界：

~~~text
t = 1001 ms
5 requests -> all allowed
~~~

因此很短的一段真实时间内可能通过 10 个请求。

~~~text
990 ms      1000 ms      1001 ms
| 5 req |      | 5 req |
~~~

这就是 Fixed Window 的边界突发问题。

English:

Fixed Window resets the counter at each boundary. Five requests near the end of one window and five near the beginning of the next may all pass within only a few milliseconds.

## 场景 2：Sliding Window 更平滑 / Sliding Window Is Smoother

Sliding Window Log 会记录最近一个时间窗口内每次请求的时间戳。

当 t=1001 ms 时，它会检查：

~~~text
(1 ms, 1001 ms]
~~~

如果前面 990 ms 的 5 个请求仍然位于最近 1000 ms 内，则新请求会被拒绝。

优点：

- 精确
- 边界更平滑

缺点：

- 每个请求都可能需要保存一个时间戳
- 高流量下内存成本更高

English:

Sliding Window Log tracks request timestamps inside the active window. It avoids the fixed-window boundary burst, but it stores more state and is more expensive at high traffic.

## 场景 3：Token Bucket 允许受控突发 / Token Bucket Allows Controlled Bursts

配置：

~~~text
capacity = 5 tokens
refill   = 5 tokens / second
~~~

Bucket 初始满：

~~~text
5 immediate requests -> allowed
6th request           -> rejected
~~~

经过 400 ms：

~~~text
refill about 2 tokens
~~~

于是可以再允许约 2 个请求。

Token Bucket 的核心是：

> 平均速率受 refill rate 控制，但允许最多 capacity 大小的短时突发。

English:

Token Bucket controls the long-term average through the refill rate while allowing a bounded burst up to the bucket capacity.

## 算法对比 / Comparison

| Algorithm | 中文特点 | English |
| --- | --- | --- |
| Fixed Window | 实现简单，但有窗口边界突发 | Simple, but has boundary bursts |
| Sliding Window Log | 更精确、更平滑，但状态更多 | Precise and smooth, but stores more state |
| Token Bucket | 适合限制平均速率，同时允许受控突发 | Controls average rate while allowing bounded bursts |

## 运行 / Run

需要 / Requires JDK 17+ and Maven.

~~~bash
mvn clean compile
java -cp target/classes dev.xbhou.javalab.ratelimit.App
~~~

## 生产设计问题 / Production Design Questions

1. 限流维度是什么？ / What is the key: user, IP, API, tenant, or global?
2. 限流发生在哪一层？ / Gateway, application, or downstream?
3. 超限后是 Reject、Queue 还是 Degrade？ / Reject, queue, or degrade?
4. 是否允许短时 Burst？ / Are short bursts acceptable?
5. Rate Limit 是否需要分布式一致？ / Must the limit be shared across instances?
6. 多机部署时状态放哪里？ / Where does shared state live?
7. Redis 操作是否原子？ / Are Redis operations atomic?
8. 限流本身失败时 fail-open 还是 fail-closed？ / Fail open or fail closed?
9. 429 响应是否带 Retry-After？ / Should HTTP 429 include Retry-After?
10. 是否有排队延迟和拒绝率监控？ / Are queue latency and rejection rate observable?

## 分布式实现 / Distributed Implementation

中文：

单机内存限流只能约束当前实例。

如果服务有 10 个实例：

~~~text
instance-1 limit = 100
...
instance-10 limit = 100
~~~

整体实际可能达到约 1000。

如果需要全局限流，通常需要：

- Redis + Lua
- Gateway 集中限流
- 专门 Rate Limiter Service
- 数据面本地限流 + 控制面动态配置

Token Bucket / Sliding Window 在 Redis 中通常需要保证“读取状态 + 更新状态 + 判断”原子完成。

English:

An in-memory limiter only controls one process. Global limits across many instances typically require a shared or centralized design such as Redis + Lua, gateway enforcement, or a dedicated rate-limiter service.

## 限流与熔断的区别 / Rate Limiting vs Circuit Breaking

~~~text
Rate Limiting
-> 防止流量超过系统计划容量
-> protect from too much incoming traffic

Circuit Breaking
-> 下游已经出现失败/变慢时停止继续调用
-> stop calling an unhealthy dependency
~~~

它们保护的方向不同，下一步 Lab 会进入 Circuit Breaker。

English:

Rate limiting protects capacity from excess incoming traffic. Circuit breaking protects callers from an already unhealthy dependency.

They solve different problems and are often used together.
