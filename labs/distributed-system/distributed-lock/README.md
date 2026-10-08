# 分布式锁实验 / Distributed Lock Lab

## 问题 / Question

中文：

分布式锁看起来很简单：

~~~text
抢锁
  ↓
执行业务
  ↓
释放锁
~~~

但真正困难的是失败场景：

- Lock Lease 过期了，但旧持有者还在执行
- 旧持有者晚到一步执行 unlock
- 新持有者的锁被旧持有者误删
- 业务执行时间超过 Lease
- 即使安全 unlock，旧持有者仍可能继续写下游资源

这个 Lab 用一个内存实现模拟 Redis 风格的 Lease Lock，专门观察这些边界。

English:

A distributed lock looks simple: acquire, execute, release.

The hard part is failure behavior: lease expiry, late unlocks, stale owners, long-running work, and stale writes that happen after ownership has already moved to another process.

This lab simulates a Redis-style lease lock in memory so those edge cases are deterministic and easy to observe.

## 核心模型 / Core Model

每次获取锁时生成三个关键属性：

~~~text
lock key
owner token
fencing token
~~~

Lock Record:

~~~text
key
ownerToken
expiresAt
fencingToken
~~~

### Owner Token

Owner Token 用来回答：

> 当前执行 unlock 的人，还是不是这个锁的持有者？

### Lease

Lease 用来避免持有者宕机后锁永久不释放。

~~~text
acquire
   |
   +---- lease expires after N ms
~~~

### Fencing Token

Fencing Token 是单调递增版本号：

~~~text
Owner A -> token 1
Owner B -> token 2
Owner C -> token 3
~~~

下游资源只接受不小于自己已见最大 token 的写操作。

它解决的问题不是“谁能释放锁”，而是：

> 一个已经失去锁的旧持有者，是否还能修改受保护资源？

---

## 场景 1：错误释放别人的锁 / Scenario 1: Unsafe Unlock Deletes Another Owner

~~~text
A acquires lock
lease = 100 ms

A is slow...

100 ms passes
lock expires

B acquires same lock

A finally finishes
A executes DEL key

B's lock is deleted

C can now acquire
even though B is still working
~~~

如果 release 只是：

~~~text
DEL lock-key
~~~

就可能删除新持有者 B 的锁。

这个问题非常经典。

English:

If unlock simply deletes the lock key, an old owner can delete a newer owner's lock after its own lease has expired.

## 场景 2：Owner Token 安全释放 / Scenario 2: Owner-Checked Release

正确释放逻辑应该类似：

~~~text
if current.ownerToken == my.ownerToken:
    delete lock
else:
    do nothing
~~~

Redis 中通常不能写成 GET + DEL 两条独立命令，因为中间可能发生竞态。

典型 Redis 实现会使用 Lua Script 原子执行：

~~~lua
if redis.call("get", KEYS[1]) == ARGV[1] then
    return redis.call("del", KEYS[1])
else
    return 0
end
~~~

这个 Lab 用原子 map operation 模拟相同思想。

Owner Token 可以防止：

> A 的 late unlock 删除 B 的锁。

但请注意：

> 它不能解决 A 在 Lease 过期后仍继续执行业务的问题。

---

## 场景 3：Lease Renewal / Watchdog

如果正常业务可能执行 500 ms，而 Lease 只有 100 ms：

~~~text
A acquire
    |
100 ms
    |
lock expires
    |
B acquire
~~~

此时 A 与 B 可能同时处于 Critical Section。

一种常见做法是 Lease Renewal：

~~~text
A acquires lease
    |
    +-- renew
    |
    +-- renew
    |
    +-- renew
    |
business finishes
    |
release
~~~

Redisson Watchdog 就属于类似思路。

但 Renewal 也不是绝对安全：

- Process Stop-The-World
- Network Partition
- Scheduler Delay
- Redis Unavailable
- Application Pause

都可能导致 Renewal 来不及。

所以：

> Lease Renewal 提高可用性，但不能从数学上消除 stale owner。

---

## 场景 4：Fencing Token 防止 Stale Write / Scenario 4: Fencing Token

这是更关键的一步。

~~~text
A acquires lock -> fencing token 1

A pauses
lease expires

B acquires lock -> fencing token 2
B writes resource
resource remembers token 2

A resumes
A writes with token 1

resource rejects token 1
~~~

关键点：

~~~text
lock ownership moved forward
        ↓
resource version also moves forward
        ↓
stale owner cannot write backward
~~~

这要求真正被保护的下游资源能够理解 Fencing Token。

例如可以通过：

- 数据库 version 字段
- Compare-And-Set
- Conditional Update
- 单调 sequence
- 下游支持 fencing/version contract

实现。

---

## 运行 / Run

需要 / Requires JDK 17+ and Maven.

~~~bash
mvn clean compile
java -cp target/classes dev.xbhou.javalab.lock.App
~~~

---

## Redis 中常见实现 / Common Redis Pattern

获取锁通常类似：

~~~text
SET resource-key owner-token NX PX 10000
~~~

含义：

~~~text
NX -> key 不存在才成功
PX -> 设置 Lease
value -> owner token
~~~

释放锁必须校验 owner token，并且校验与删除应该原子完成。

---

## 为什么不能只设置一个超长 Lease / Why Not Just Use a Huge Lease?

中文：

超长 Lease 会降低故障恢复速度。

如果进程在持锁后直接崩溃：

~~~text
lease = 10 minutes
~~~

其他实例可能需要等 10 分钟才能继续。

Lease 本质是一个权衡：

~~~text
太短
-> 容易业务没执行完就过期

太长
-> 宕机后恢复慢
~~~

English:

A very long lease reduces accidental expiry but slows recovery when the lock owner crashes.

Lease duration is a trade-off between premature expiry and failure recovery time.

## 分布式锁不解决什么 / What a Distributed Lock Does Not Solve

分布式锁并不会自动解决：

- 幂等
- Transaction
- Exactly Once
- 下游重复请求
- DB 数据一致性
- Message Duplicate
- 长事务
- Network Partition

很多场景其实更适合：

- Database Unique Constraint
- Optimistic Lock
- CAS
- Idempotency Key
- Queue Serialization
- Single Writer
- State Machine

而不是直接上 Distributed Lock。

---

## 生产设计问题 / Production Design Questions

设计分布式锁时至少问：

1. 为什么这里真的需要 Lock？ / Why is a lock necessary here?
2. 能不能用 Unique Constraint 或 CAS？ / Could a unique constraint or CAS solve it instead?
3. Lease 多久？ / How long should the lease be?
4. 谁负责 Renewal？ / Who renews the lease?
5. Renewal 失败怎么办？ / What happens if renewal fails?
6. Unlock 是否校验 Owner Token？ / Is unlock owner-checked?
7. Unlock 是否原子？ / Is unlock atomic?
8. Lease 过期后旧 Owner 还能做什么？ / What can a stale owner still do?
9. 是否需要 Fencing Token？ / Is fencing required?
10. 下游资源能否拒绝 stale token？ / Can the protected resource reject stale tokens?
11. Redis Cluster / Failover 会有什么语义？ / What are the failover semantics?
12. Lock contention 如何监控？ / How is contention observed?

---

## 与前面实验的关系 / Relationship to Earlier Labs

~~~text
Retry
  ↓
Idempotency
  ↓
Distributed Lock
~~~

中文：

这三个概念解决的问题不同：

- Retry：失败后何时再次尝试
- Idempotency：重复尝试不会产生额外副作用
- Distributed Lock：多个执行者之间如何协调某段共享资源访问

不要把它们互相替代。

English:

These concepts solve different problems:

- Retry decides when to attempt again.
- Idempotency makes repeated attempts safe.
- Distributed locking coordinates concurrent access to a shared resource.

They complement each other; they are not substitutes.
