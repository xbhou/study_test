# 幂等实验 / Idempotency Lab

## 问题 / Question

中文：

当客户端请求已经在服务端成功执行，但响应因为网络问题丢失时，客户端通常只看到 timeout。它无法判断“请求没有执行”还是“已经执行成功，只是响应没回来”。

如果客户端直接 Retry，而服务端没有幂等保护，就可能产生重复扣款、重复下单、重复转账或重复创建资源。

English:

When a request succeeds on the server but the response is lost, the client only sees a timeout. It cannot know whether the operation never ran or succeeded and only lost its response.

Blind retries can therefore create duplicate charges, orders, transfers, or resources.

本实验使用 / This lab uses:

- Idempotency Key
- Request Fingerprint
- PROCESSING / SUCCESS state
- Cached Result Replay

## 核心模型 / Core Model

中文：

客户端为一次“逻辑操作”生成唯一 Idempotency Key。服务端保存 Key、请求指纹、执行状态和最终结果。

~~~text
Idempotency-Key: payment-order-1001

key
request fingerprint
status: PROCESSING / SUCCESS
result
~~~

同一个 Key 再次到达时，服务端不应该直接重新执行 Side Effect，而是先判断现有状态。

English:

The client generates one Idempotency Key for one logical operation. The server stores the key, request fingerprint, processing state, and final result.

When the same key arrives again, the server should inspect the existing record instead of blindly executing the side effect again.

## 场景 1：成功但响应丢失 / Scenario 1: Success but Response Lost

中文：

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

第二次请求直接复用第一次的成功结果，不再次执行扣款。

English:

The first request executes the side effect once and stores the successful result.

When the client retries with the same key and same payload, the server replays the previous result instead of charging again.

## 场景 2：并发重复请求 / Scenario 2: Concurrent Duplicate Request

中文：

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

第一个请求仍在处理时，第二个相同请求不能再次执行 Side Effect。

这个 Demo 返回 IN_PROGRESS。真实 API 也可能选择 HTTP 202、HTTP 409、查询处理状态，或者短暂等待第一个请求完成。

English:

If the first request still owns the key and is processing, a concurrent duplicate must not execute the side effect again.

This demo returns IN_PROGRESS. A real API could instead return HTTP 202, HTTP 409, a processing status, or briefly wait for the first request.

## 场景 3：同 Key 不同参数 / Scenario 3: Same Key, Different Payload

中文：

~~~text
key = K
amount = 10.00
~~~

之后错误复用：

~~~text
key = K
amount = 20.00
~~~

这两个请求不能被视为同一个逻辑请求。

因此除了 Idempotency Key，还要保存 Request Fingerprint：

~~~text
fingerprint(old request) != fingerprint(new request)
                      |
                      v
                   CONFLICT
~~~

English:

An Idempotency Key must not silently map different payloads to the same result.

The server stores a request fingerprint and returns CONFLICT when the same key is reused with different parameters.

## 原子占用 Key / Atomic Key Ownership

中文：

下面这种写法存在竞态：

~~~java
if (!map.containsKey(key)) {
    executeSideEffect();
    map.put(key, result);
}
~~~

两个线程可能同时通过 containsKey，然后都执行 Side Effect。

本实验通过 ConcurrentHashMap.putIfAbsent 原子决定谁是这个 Key 的第一个执行者。

English:

A check-then-act pattern using containsKey is racy because two threads may both pass the check.

The lab uses ConcurrentHashMap.putIfAbsent so ownership of a new key is decided atomically.

## 为什么需要状态 / Why State Matters

至少要区分：

~~~text
PROCESSING
SUCCESS
~~~

中文：

如果只知道“Key 存在”，就无法区分第一次请求仍在执行，还是已经执行成功。

生产环境可能进一步设计：

~~~text
PROCESSING
SUCCESS
FAILED_RETRYABLE
FAILED_FINAL
EXPIRED
~~~

English:

Knowing that a key exists is not enough. The service needs to know whether the original request is still processing or has already completed successfully.

A production state machine may also include retryable failure, final failure, and expiration.

## 运行 / Run

需要 / Requires JDK 17+ and Maven.

~~~bash
mvn clean compile
java -cp target/classes dev.xbhou.javalab.idempotency.App
~~~

## 生产存储模型 / Production Storage Model

一个简化表结构 / A simplified schema:

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

中文：

真正关键的是第一次占用 Idempotency Key 必须原子完成。数据库中通常依赖 UNIQUE / PRIMARY KEY、INSERT ... ON CONFLICT、INSERT IGNORE 或其他 Conditional Write，而不是先 SELECT 再 INSERT。

English:

The critical requirement is that claiming a new Idempotency Key must be atomic. In a database this normally relies on a unique constraint or another conditional-write primitive, not SELECT followed by INSERT.

## 事务边界 / Transaction Boundary

中文：

如果业务数据和 Idempotency Record 在同一个数据库，通常应该放进同一个本地事务，否则可能出现：

~~~text
业务操作成功
      |
      X
幂等记录没有成功写入
~~~

如果 Side Effect 位于外部系统，例如 Payment Provider、Blockchain 或第三方 API，本地 DB Transaction 无法覆盖整个过程。

此时还需要考虑：

- 下游自己的 Idempotency Key
- Outbox
- 状态机
- 最终状态查询
- Compensation
- Reconciliation

English:

If business data and the idempotency record live in the same database, they should usually share one local transaction.

If the side effect is external, a local transaction is not enough. Designs may require downstream idempotency, an outbox, a state machine, reconciliation, or compensation.

## 幂等不等于 Exactly Once / Idempotency Is Not Exactly Once

中文：

网络仍然可能丢包、超时、重复、延迟或乱序。

幂等的核心不是保证消息只发送一次，而是：

> 同一个逻辑请求重复到达时，不产生新的业务副作用。

English:

The network may still drop, duplicate, delay, or reorder messages.

Idempotency does not guarantee exactly-once network delivery. It makes repeated delivery of the same logical request safe.

## 生产设计问题 / Production Design Questions

1. Idempotency Key 谁生成？ / Who generates the key?
2. Key 的作用域是什么？ / What is its scope?
3. Key 保存多久？ / How long is it retained?
4. Request Fingerprint 包含哪些字段？ / Which fields form the fingerprint?
5. 同 Key 不同参数怎么处理？ / What happens for the same key with different parameters?
6. PROCESSING 状态下重复请求怎么处理？ / How are duplicates handled while processing?
7. SUCCESS 结果保存多久？ / How long is a successful result cached?
8. 失败后是否允许同 Key Retry？ / Can failed requests reuse the same key?
9. 幂等记录和业务事务是否原子？ / Is the idempotency record atomic with the business transaction?
10. 下游 Side Effect 是否也支持幂等？ / Does the downstream side effect support idempotency?

## 与 Retry 的关系 / Relationship to Retry

中文：

前一个实验回答：

> 什么时候应该 Retry？

这个实验回答：

> Retry 发生时，如何避免重复 Side Effect？

实际系统通常需要把下面三个能力一起设计：

~~~text
Retry Policy
    +
Idempotency
    +
Deadline
~~~

English:

The retry lab asks when another attempt should happen. This lab asks how to prevent duplicate side effects when that retry occurs.

Retry, idempotency, and deadline budgeting should therefore be designed together.
