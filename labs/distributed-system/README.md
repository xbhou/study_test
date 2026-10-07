# Distributed Systems / 分布式系统

## 中文

这里收录后端分布式系统中的小型可运行实验，包括幂等、重试、限流、熔断、路由和聚合。

### Labs

#### 幂等 / Idempotency

路径：`idempotency/`

重点：

- Idempotency Key
- Request Fingerprint
- PROCESSING / SUCCESS 状态
- 请求成功但响应丢失后的安全 Retry
- 并发重复请求
- 同 Key 不同参数冲突
- 为什么 Idempotency 不等于 Exactly Once

#### Retry + Exponential Backoff + Jitter + Deadline

路径：`retry-backoff-deadline/`

重点：

- 可重试与不可重试异常
- Exponential Backoff
- Full Jitter
- Deadline Budget
- Retry Amplification
- 为什么必须明确 Retry Ownership

Retry 和 Idempotency 应该一起设计：Retry 决定“何时再次尝试”，Idempotency 保证“再次尝试不会制造新的业务副作用”。

---

## English

Small system-design experiments for idempotency, retries, rate limiting, circuit breaking, routing, and aggregation.

### Labs

#### Idempotency / 幂等

Path: `idempotency/`

Focus:

- idempotency keys
- request fingerprints
- PROCESSING / SUCCESS state
- safe retry after a lost response
- concurrent duplicate requests
- same-key / different-payload conflicts
- why idempotency is not the same as exactly-once delivery

#### Retry + Exponential Backoff + Jitter + Deadline

Path: `retry-backoff-deadline/`

Focus:

- retryable vs non-retryable failures
- exponential backoff
- full jitter
- deadline budgeting
- retry amplification
- why retry ownership must be explicit

Retry and idempotency should be designed together: retry decides when to attempt again, while idempotency prevents duplicate business side effects.
