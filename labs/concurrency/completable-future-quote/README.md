# CompletableFuture Quote Aggregator / 并行报价聚合

## 中文

### 问题

一个服务如何并行请求多个独立 Provider，忽略慢请求和失败请求，并从有效结果中选出最优报价？

### 设计

```text
Quote Request
     |
     v
+-------------------+
| QuoteAggregator   |
+-------------------+
   |      |      |
   v      v      v
Provider A B      C
   \      |      /
    \-- concurrent --/
            |
            v
      timeout / errors
            |
            v
     valid quote list
            |
            v
       best quote
```

### 这个实验演示什么

- `CompletableFuture.supplyAsync`
- 独立线程池
- `orTimeout`
- 异常隔离
- `allOf`
- 结果归集
- 最优报价选择

### 运行

需要 JDK 17+ 和 Maven。

```bash
mvn clean compile
java -cp target/classes dev.xbhou.javalab.quote.App
```

### 观察重点

Provider C 会故意超过 timeout，但它不应该阻止其他 Provider 返回结果。

核心设计思想是：

> 一个 Provider 失败，应该减少可选候选结果，而不是让整个聚合请求直接失败。

### 后续实验

参见 `../completable-future-deadline/`：

- Overall Request Deadline
- Failure Isolation
- Partial Results
- Cancellation Semantics

未来还可以继续：

- Retry with Backoff
- Provider Health Score
- Dynamic Routing
- Redis Quote Cache

---

## English

### Question

How can a service request several independent providers concurrently, ignore slow or failed providers, and return the best valid quote?

### Design

```text
Quote Request
     |
     v
+-------------------+
| QuoteAggregator   |
+-------------------+
   |      |      |
   v      v      v
Provider A B      C
   \      |      /
    \-- concurrent --/
            |
            v
      timeout / errors
            |
            v
     valid quote list
            |
            v
       best quote
```

### What this lab demonstrates

- `CompletableFuture.supplyAsync`
- a dedicated executor
- `orTimeout`
- exception isolation
- `allOf`
- result aggregation
- best-price selection

### Run

Requires JDK 17+ and Maven.

```bash
mvn clean compile
java -cp target/classes dev.xbhou.javalab.quote.App
```

### Things to observe

Provider C intentionally exceeds the timeout and should not prevent the other providers from producing a result.

The important design idea is that one provider failure should degrade the candidate set, not fail the whole aggregation request.

### Follow-up Lab

See `../completable-future-deadline/` for:

- overall request deadline
- failure isolation
- partial results
- cancellation semantics

Future experiments:

- retry with backoff
- provider health score
- dynamic routing
- Redis quote cache
