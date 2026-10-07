# Idempotency Lab / 幂等实验

## 中文

### 问题

如果客户端请求已经在服务端成功执行，但响应在网络中丢失，客户端通常只能看到 timeout。

它无法确定：

~~~text
到底没执行？
还是已经执行成功，只是响应没回来？
~~~

如果客户端直接重试，而服务端没有幂等保护，就可能产生：

- 重复扣款
- 重复下单
- 重复转账
- 重复发券
- 重复创建资源

本实验演示一个基于 **Idempotency Key + Request Fingerprint + 状态记录 + 结果复用** 的最小模型。

### 核心模型

客户端为一次逻辑操作生成唯一 Key：

~~~text
Idempotency-Key: payment-order-1001
~~~

服务端保存：

~~~text
key
request fingerprint
status: PROCESSING / SUCCESS
result
~~~

同一个 Key 再次到达时，不是重新执行，而是判断当前状态。

### 场景 1：第一次已成功，但响应丢失

~~~text
Client
  |
  | request(key=K)
  v
Server
  |
  | charge once
  v
SUCCESS
  |
  X response lost

Client timeout
  |
  | retry(key=K)
  v
Server
  |
  | find SUCCESS record
  | reuse previous result
  v
same PaymentResult
~~~

关键结果：

~~~text
logical requests = 2
actual charge     = 1
~~~

这就是幂等最常见的价值。

### 场景 2：重复请求在第一次仍处于 PROCESSING 时到达

~~~text
Request A(key=K)
    |
    v
PROCESSING
    |
    +---- Request B(key=K)
              |
              v
          IN_PROGRESS
~~~

第二个请求不能再次执行 Side Effect。

本实验选择返回 IN_PROGRESS。真实 API 也可以选择：

- HTTP 409
- HTTP 202
- 返回当前处理状态
- 短暂等待第一个请求完成

具体策略取决于 API Contract。

### 场景 3：相同 Key，但请求参数不同

例如：

~~~text
key = K
amount = 10.00
~~~

之后客户端错误地复用同一个 Key：

~~~text
key = K
amount = 20.00
~~~

这不能被当成相同请求。

所以服务端除了保存 Idempotency Key，还要保存 Request Fingerprint：

~~~text
fingerprint(old request) != fingerprint(new request)
                      |
                      v
                   CONFLICT
~~~

否则一个旧 Key 可能错误复用另一个请求的结果。

### 为什么不能只用 containsKey

这种写法有竞态：

~~~java
if (!map.containsKey(key)) {
    executeSideEffect();
    map.put(key, result);
}
~~~

两个线程可能同时通过 containsKey 检查，然后都执行 Side Effect。

本实验使用 putIfAbsent 来原子地决定谁是这个 Key 的第一个执行者。

### 状态为什么重要

仅仅记录“Key 出现过”不够。

至少要区分：

~~~text
PROCESSING
SUCCESS
~~~

否则并发重复请求到来时，服务端无法判断：

> 应该复用结果，还是第一次请求仍在执行？

更完整的生产实现还可能有：

~~~text
PROCESSING
SUCCESS
FAILED_RETRYABLE
FAILED_FINAL
EXPIRED
~~~

状态设计取决于业务语义。

### 运行

需要 JDK 17+ 和 Maven。

~~~bash
mvn clean compile
java -cp target/classes dev.xbhou.javalab.idempotency.App
~~~

### 生产数据库模型

一个简化表结构可以是：

~~~sql
CREATE TABLE idempotency_record (
    idempotency_key VARCHAR(128) PRIMARY KEY,
    request_hash    VARCHAR(128) NOT NULL,
    status          VARCHAR(32)  NOT NULL,
    response_data   TEXT,
    created_at      TIMESTAMP    NOT NULL,
    expire_at       TIMESTAMP    NOT NULL
);
~~~

真正关键的是：

> 对 Idempotency Key 的第一次占用必须是原子的。

数据库里通常依赖：

- UNIQUE / PRIMARY KEY
- INSERT ... ON CONFLICT
- INSERT IGNORE
- Conditional Write

而不是先 SELECT 再 INSERT。

### Idempotency Record 和业务事务

一个更困难的问题是：

~~~text
业务操作成功
      |
      X
幂等记录没有成功写入
~~~

或者反过来：

~~~text
幂等记录写 SUCCESS
      |
      X
业务操作其实失败
~~~

所以如果 Idempotency Record 和业务数据都在同一个数据库，最好放进同一个本地事务。

如果 Side Effect 在外部系统，例如：

~~~text
Payment Provider
Blockchain
第三方 API
~~~

就不能简单依赖本地 DB Transaction，需要结合：

- 下游自己的 Idempotency Key
- Outbox
- 状态机
- 查询最终状态
- 补偿
- 对账

### 幂等不等于 Exactly Once

幂等的目标通常是：

> 同一个逻辑请求重复到达，不产生新的业务副作用。

它并不意味着分布式系统真的实现了神奇的 Exactly Once Network Delivery。

网络依旧可能：

- 丢包
- 超时
- 重复
- 乱序

幂等是让系统能够**安全处理重复**。

### 生产设计问题

设计幂等接口时至少问：

1. Idempotency Key 谁生成？
2. Key 的作用域是什么？
3. Key 保存多久？
4. Request Fingerprint 包含哪些字段？
5. 同 Key 不同参数怎么处理？
6. PROCESSING 状态下重复请求怎么处理？
7. SUCCESS 结果保存多久？
8. 失败后是否允许同 Key 重试？
9. 幂等记录和业务事务是否原子？
10. 下游 Side Effect 是否也支持幂等？

### 与 Retry 的关系

前一个实验研究：

~~~text
什么时候应该 Retry？
~~~

这个实验回答：

~~~text
Retry 发生时，如何避免重复 Side Effect？
~~~

两者应该一起设计：

~~~text
Retry Policy
    +
Idempotency
    +
Deadline
~~~

---

## English

### Question

If a request succeeds on the server but the response is lost, the client only observes a timeout.

It cannot know whether the operation never ran or succeeded and only lost its response.

Blind retries can therefore create duplicate charges, orders, transfers, coupons, or resources.

This lab demonstrates a minimal model using:

- Idempotency Key
- Request Fingerprint
- Processing State
- Cached Result Replay

### Scenario 1: Success happened, response was lost

The first request performs the side effect once and stores the successful result.

When the client retries with the same key and same request, the server returns the original result without executing the side effect again.

~~~text
logical requests = 2
actual charge     = 1
~~~

### Scenario 2: Duplicate request arrives while processing

The first request owns the key and is still processing.

A concurrent duplicate request receives IN_PROGRESS instead of executing the side effect again.

A real API could return HTTP 409, HTTP 202, a processing status, or briefly wait for the first request depending on its contract.

### Scenario 3: Same key, different request

An Idempotency Key must not silently map different request payloads to one result.

The server stores a request fingerprint and returns CONFLICT when the same key is reused with different parameters.

### Why containsKey is not enough

This pattern is racy:

~~~java
if (!map.containsKey(key)) {
    executeSideEffect();
    map.put(key, result);
}
~~~

Two threads can both pass the check.

The lab uses putIfAbsent so ownership of a new key is atomic.

### State Matters

At minimum, the server needs to distinguish:

~~~text
PROCESSING
SUCCESS
~~~

A fuller production state machine may include retryable failure, final failure, and expiration.

### Run

~~~bash
mvn clean compile
java -cp target/classes dev.xbhou.javalab.idempotency.App
~~~

### Production Storage

A simplified database table could store the key, request hash, status, response, and expiration time.

The critical requirement is that claiming a new key must be atomic, usually through a UNIQUE / PRIMARY KEY constraint or another conditional-write primitive.

### Transaction Boundary

If the business update and idempotency record live in the same database, they should usually share a local transaction.

If the side effect is external, such as a payment provider or blockchain call, a local transaction is not enough. Designs may need downstream idempotency, an outbox, a state machine, reconciliation, or compensation.

### Idempotency Is Not Exactly Once

The network may still drop, duplicate, delay, or reorder messages.

Idempotency means repeated delivery of the same logical request does not create additional business side effects.

### Production Questions

1. Who generates the Idempotency Key?
2. What is its scope?
3. How long is it retained?
4. Which fields form the request fingerprint?
5. What happens when the same key carries different parameters?
6. What happens while the original request is still processing?
7. How long is a successful response cached?
8. Can failed requests reuse the same key?
9. Is the idempotency record atomic with the business transaction?
10. Does the downstream side effect also support idempotency?

### Relationship to Retry

The retry lab asks:

> When should we retry?

This lab asks:

> When retry happens, how do we prevent duplicate side effects?

They should be designed together with the overall request deadline.
