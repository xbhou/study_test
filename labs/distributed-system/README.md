# Distributed Systems / 分布式系统

## 中文

这里收录后端分布式系统中的小型可运行实验，包括幂等、重试、限流、熔断、路由和聚合。

### Labs

#### Retry + Exponential Backoff + Jitter + Deadline

路径：`retry-backoff-deadline/`

重点：

- 可重试与不可重试异常
- Exponential Backoff
- Full Jitter
- Deadline Budget
- Retry Amplification
- 为什么必须明确 Retry Ownership

下一步主题是 **Idempotency（幂等）**，因为只有重复执行不会产生额外副作用时，重试才真正安全。

---

## English

Small system-design experiments for idempotency, retries, rate limiting, circuit breaking, routing, and aggregation.

### Labs

#### Retry + Exponential Backoff + Jitter + Deadline

Path: `retry-backoff-deadline/`

Focus:

- retryable vs non-retryable failures
- exponential backoff
- full jitter
- deadline budgeting
- retry amplification
- why retry ownership must be explicit

The next natural topic is **idempotency**, because retries are only safe when repeated execution cannot create duplicate side effects.
