# 线程转储分析实验 / Thread Dump Analysis Lab

## 目标 / Goal

中文：

这个 Lab 故意制造多种典型线程状态和故障场景，然后用 `jcmd Thread.print` / `jstack` 做真实排查：

- RUNNABLE
- BLOCKED
- WAITING
- TIMED_WAITING
- Java-level Deadlock
- Thread Pool Saturation / Queue Backlog

English:

This lab deliberately creates common thread states and failure patterns, then inspects them with `jcmd Thread.print` and `jstack`.

## 线程状态心智模型 / Thread State Mental Model

~~~text
NEW
 |
 v
RUNNABLE
 |   |    waiting for monitor
 |    v
 |  BLOCKED
 |
 +--> WAITING
 |
 +--> TIMED_WAITING
 |
 v
TERMINATED
~~~

注意：

> Java 的 RUNNABLE 不等于“此刻一定正在 CPU 上执行”。

JVM 的 RUNNABLE 包含“正在运行”以及“可运行、等待 OS 调度”等情况。

English:

Java RUNNABLE does not guarantee that the thread is physically executing on a CPU at that exact moment. It represents the JVM runnable state.

## 实验线程 / Lab Threads

程序会创建这些有明确名字的线程：

~~~text
lab-runnable-cpu
lab-lock-holder
lab-blocked-waiter
lab-waiting-latch
lab-timed-sleeper
lab-deadlock-a
lab-deadlock-b
lab-pool-worker-1
~~~

这使 Thread Dump 更容易检索。

## 场景 1：RUNNABLE

`lab-runnable-cpu` 持续执行计算：

~~~text
while (running) {
    compute();
}
~~~

Thread Dump 里通常看到：

~~~text
"lab-runnable-cpu" ... RUNNABLE
    at ...ThreadDumpLab.cpuLoop(...)
~~~

中文：

如果线上 CPU 很高，可以把 Thread Dump 和 OS CPU 线程信息结合起来，例如：

~~~bash
top -H -p <pid>
~~~

找到高 CPU 的 native thread id，再与 Java Thread Dump 对照。

English:

For high-CPU incidents, combine thread dumps with OS per-thread CPU data such as `top -H -p <pid>`.

## 场景 2：BLOCKED

`lab-lock-holder` 先进入：

~~~java
synchronized (lock) {
    ...
}
~~~

并长时间持有 monitor。

随后 `lab-blocked-waiter` 也尝试进入同一个 monitor：

~~~text
holder owns monitor
       |
       v
waiter wants monitor
       |
       v
BLOCKED
~~~

Thread Dump 中重点看：

~~~text
"lab-blocked-waiter" ... BLOCKED
    - waiting to lock <0x...>

"lab-lock-holder" ...
    - locked <0x...>
~~~

两个相同的 monitor address 能把“谁在等”和“谁持有”关联起来。

English:

The waiter is BLOCKED on a monitor owned by another thread. Matching monitor identities connects the blocked thread to the lock owner.

## 场景 3：WAITING

`lab-waiting-latch` 调用：

~~~java
CountDownLatch.await();
~~~

没有 timeout，因此通常是：

~~~text
WAITING
~~~

Thread Dump 可能看到：

~~~text
parking to wait for <0x...>
java.util.concurrent.CountDownLatch.await
~~~

WAITING 常见来源：

- Object.wait()
- CountDownLatch.await()
- Future.get() without timeout
- LockSupport.park()
- Thread.join() without timeout

## 场景 4：TIMED_WAITING

`lab-timed-sleeper` 调用：

~~~java
Thread.sleep(...)
~~~

因此通常看到：

~~~text
TIMED_WAITING (sleeping)
~~~

常见来源：

- Thread.sleep
- Object.wait(timeout)
- Thread.join(timeout)
- LockSupport.parkNanos / parkUntil

## 场景 5：Deadlock

两个线程故意形成经典死锁：

~~~text
Thread A:
lock A
  |
wait for B

Thread B:
lock B
  |
wait for A
~~~

形成：

~~~text
A owns lock-A, waits lock-B
B owns lock-B, waits lock-A
~~~

使用：

~~~bash
jcmd <pid> Thread.print -l
~~~

或者：

~~~bash
jstack -l <pid>
~~~

HotSpot 通常会在末尾直接报告：

~~~text
Found one Java-level deadlock
~~~

并列出参与死锁的线程和 monitor。

## 场景 6：Thread Pool Saturation / Queue Backlog

Lab 创建：

~~~text
corePoolSize = 1
maxPoolSize  = 1
queue        = 4
~~~

Worker 执行一个不会立即结束的任务：

~~~text
active worker = 1
queued tasks  = 4
~~~

程序会输出：

~~~text
poolSize=1
active=1
queueSize=4
~~~

### 为什么 Thread Dump 看不到 4 个 queued tasks？

因为：

> Queue 中的 Runnable 还不是正在运行的 Thread。

Thread Dump 主要展示线程及其 stack，不会把 Executor Queue 中每个 Runnable 当成独立线程显示。

所以排查线程池堆积需要组合：

~~~text
Thread Dump
+
ThreadPoolExecutor metrics
+
queue depth
+
active count
+
task latency
+
rejection count
~~~

English:

Queued executor tasks are not threads, so they do not appear as individual thread stacks. Thread-pool diagnosis needs both thread dumps and executor metrics.

## 运行 / Run

需要 / Requires JDK 17+ and Maven.

~~~bash
mvn clean compile
java -cp target/classes dev.xbhou.javalab.threaddump.ThreadDumpLab
~~~

程序默认保持约 60 秒，启动后会打印：

~~~text
PID=12345
~~~

以及建议命令。

如果想修改观察时间：

~~~bash
java   -cp target/classes   dev.xbhou.javalab.threaddump.ThreadDumpLab 120
~~~

参数单位是秒。

## 使用 jcmd / Inspect with jcmd

先启动 Lab：

~~~bash
java -cp target/classes dev.xbhou.javalab.threaddump.ThreadDumpLab
~~~

得到：

~~~text
PID=12345
~~~

另开终端：

~~~bash
jcmd 12345 Thread.print -l
~~~

可以先只筛选 Lab 线程：

~~~bash
jcmd 12345 Thread.print -l   | grep -A 15 -E 'lab-runnable|lab-blocked|lab-waiting|lab-timed|lab-deadlock|lab-pool'
~~~

## 使用 jstack / Inspect with jstack

~~~bash
jstack -l 12345
~~~

查 Deadlock：

~~~bash
jstack -l 12345   | grep -A 40 "Found one Java-level deadlock"
~~~

## 一个 Thread Dump 应该怎么看 / Reading Workflow

拿到线上 Thread Dump，不建议从第一行逐字读。

先按问题类型定位。

### CPU 高

先找：

~~~text
RUNNABLE
~~~

再结合 OS CPU 使用率。

重点看：

- 是否多个线程卡在同一段 CPU-heavy code
- 是否死循环
- 是否序列化 / 正则 / 加密 / 压缩耗 CPU
- 是否 GC 本身占 CPU

### 请求卡住 / Latency High

先找大量：

~~~text
BLOCKED
WAITING
TIMED_WAITING
~~~

并观察它们是否集中在：

- 同一个 monitor
- 同一个 DB Client
- HTTP Client
- Connection Pool
- Future.get
- CountDownLatch
- ThreadPool queue / worker

### 怀疑死锁

优先：

~~~bash
jcmd <pid> Thread.print -l
~~~

看末尾的 deadlock detection。

### 线程数爆炸

先看：

~~~bash
jcmd <pid> Thread.print
~~~

线程总量与重复线程名。

再结合：

~~~bash
jcmd <pid> VM.native_memory summary
~~~

因为每个 Platform Thread 都需要 Stack / native memory。

## 连续抓 3 份 Thread Dump / Why Take Multiple Dumps

线上问题通常不应该只抓一份。

推荐间隔几秒抓：

~~~text
dump-1
  |
3-10 seconds
  |
dump-2
  |
3-10 seconds
  |
dump-3
~~~

原因：

一份 Dump 只能告诉你“这一瞬间”。

连续 Dump 可以判断：

- Stack 是否一直不变
- 某线程是否持续 RUNNABLE
- BLOCKED 是否长期存在
- 请求是否只是暂时等待
- Deadlock / starvation 是否稳定存在

English:

Multiple dumps reveal whether a stack is persistently stuck or only temporarily observed in one state.

## BLOCKED 和 WAITING 的关键区别 / BLOCKED vs WAITING

简化理解：

~~~text
BLOCKED
-> 想进入 synchronized monitor
-> 但 monitor 被别人占着

WAITING
-> 已经主动进入等待
-> 等其他线程 signal / unpark / countDown / complete
~~~

所以看到 BLOCKED，第一反应应该是：

> 谁持有我想拿的 monitor？

看到 WAITING，第一反应应该是：

> 我在等哪个条件或哪个异步结果？

## RUNNABLE 也可能是 I/O 吗？ / Can RUNNABLE Be I/O?

是的，不能机械地认为：

~~~text
RUNNABLE = CPU problem
~~~

某些 native I/O 场景在 Thread Dump 中也可能表现为 RUNNABLE。

因此线上诊断必须组合：

- Thread Dump
- CPU
- Network / DB metrics
- application metrics
- tracing

## Thread Dump 与 GC / Thread Dumps vs GC

如果所有业务线程都像“突然停住”，不一定是业务锁。

还要结合 GC Log 判断是否有长时间 Stop-The-World Pause。

~~~text
application latency spike
      |
      +-- lock contention?
      +-- downstream waiting?
      +-- thread-pool saturation?
      +-- GC pause?
~~~

这也是为什么前一个 GC Lab 和本 Lab 应该连续学习。

## 生产排查 Checklist / Production Checklist

1. JVM PID 是什么？
2. 当前线程数量是否异常？
3. 哪些 Thread Name 数量最多？
4. RUNNABLE 是否集中在相同 Stack？
5. BLOCKED 在等哪个 monitor？
6. 谁持有这个 monitor？
7. WAITING 在等什么条件？
8. 有没有 Java-level Deadlock？
9. Thread Pool active / queue / reject 情况如何？
10. 是否有 DB / HTTP Connection Pool 耗尽？
11. 是否同时发生长 GC Pause？
12. 连续三份 Dump 是否保持相同 Stack？

## 与前面 JVM Labs 的关系 / Relationship to Earlier JVM Labs

~~~text
Class Loading
     |
Memory Regions
     |
GC Observation
     |
Thread Dump Analysis
     |
JVM Troubleshooting
~~~

中文：

到这里，JVM 学习开始从“运行机制”进入“线上排障”。

English:

At this point the JVM track moves from runtime mechanics into practical production troubleshooting.

## 下一步 / Next Step

Phase 2 的 Roadmap 到这里完成。

下一阶段建议进入 Spring：

> Bean Lifecycle / Bean 生命周期

这样可以把前面的 Class Loading、Reflection、Threading 和 JVM 基础逐渐连接到 Java 后端框架运行机制。
