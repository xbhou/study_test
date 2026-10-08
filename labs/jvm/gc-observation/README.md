# GC 观察实验 / GC Observation Lab

## 目标 / Goal

中文：

这个 Lab 的目标不是背诵“Minor GC、Young GC、Full GC”这些名词，而是通过真实 GC 日志回答：

- 短命对象为什么会频繁触发 Young GC？
- Survivor / Object Age 是怎么变化的？
- 什么情况下对象会晋升到 Old / Tenured？
- GC 日志里的 `13M->2M(62M)` 怎么读？
- `System.gc()` 触发的 Full GC 和普通 Young GC 有什么区别？
- 为什么同一段代码换一个 Collector，日志格式和行为会不同？

English:

This lab turns GC concepts into observable runtime behavior. It focuses on young collections, survivor aging, promotion, old-generation growth, full collections, and how to read unified GC logs.

## 为什么第一版使用 SerialGC / Why This Lab Starts with SerialGC

中文：

JDK 17 在大多数常见服务端环境中默认使用 G1，但本 Lab 第一版**主动使用 SerialGC**：

~~~text
-XX:+UseSerialGC
~~~

原因不是因为推荐生产使用 SerialGC，而是它的日志非常直观：

~~~text
Eden
Survivor
Tenured
~~~

更适合先建立代际 GC 的基础心智模型。

理解完 SerialGC 后，再看 G1 的 Region、Evacuation、Humongous Object 会容易很多。

English:

JDK 17 commonly uses G1 by default on server-class machines, but this teaching lab explicitly selects SerialGC because its Eden / Survivor / Tenured behavior is easier to read.

This is a learning choice, not a production recommendation.

## 实验内存布局 / Experimental Heap Layout

推荐启动参数：

~~~bash
-Xms64m
-Xmx64m
-XX:+UseSerialGC
-XX:NewSize=16m
-XX:MaxNewSize=16m
-XX:MaxTenuringThreshold=3
~~~

简化理解：

~~~text
64 MiB Heap
|
+-- Young Generation ~16 MiB
|   |
|   +-- Eden
|   +-- Survivor From
|   +-- Survivor To
|
+-- Old / Tenured Generation ~48 MiB
~~~

## 实验代码做了什么 / What the Demo Does

每一轮：

~~~text
创建大量 128 KiB 临时对象
        |
        v
大部分很快失去引用
        |
        v
制造 Eden allocation pressure
~~~

同时每轮保留一个：

~~~text
512 KiB long-lived byte[]
~~~

所以对象分成两类：

~~~text
short-lived objects
-> should die young

retained objects
-> survive multiple Young GCs
-> may move through Survivor
-> may eventually be promoted
~~~

这就是典型的 Generational Hypothesis：

> 大多数对象朝生夕死，少量对象存活更久。

English:

Each round creates many short-lived 128 KiB arrays and retains one 512 KiB array.

The short-lived objects create allocation pressure, while retained objects survive collections and may age or promote into the old generation.

## 运行 / Run

需要 / Requires JDK 17+ and Maven.

~~~bash
mvn clean compile
~~~

然后运行：

~~~bash
java   -Xms64m   -Xmx64m   -XX:+UseSerialGC   -XX:NewSize=16m   -XX:MaxNewSize=16m   -XX:MaxTenuringThreshold=3   -Xlog:gc*,gc+age=trace   -cp target/classes   dev.xbhou.javalab.gc.GcLifecycleDemo
~~~

## 重点日志 1：Young GC / Young Collection

典型日志：

~~~text
GC(0) Pause Young (Allocation Failure)
GC(0) DefNew: 13104K(14784K)->697K(14784K)
GC(0) Tenured: 1073K(49152K)->1073K(49152K)
GC(0) Pause Young (Allocation Failure) 13M->2M(62M) 1.688ms
~~~

逐段理解：

### `GC(0)`

这是 JVM 为这次 GC 分配的事件编号。

后续：

~~~text
GC(1)
GC(2)
GC(3)
~~~

表示新的 GC 事件。

### `Pause Young`

表示这次主要回收 Young Generation。

### `Allocation Failure`

这里的含义不是 JVM “坏了”。

它表示：

> 当前 Young Generation 已经无法满足新的对象分配，因此需要先进行 GC。

### `13M->2M(62M)`

可以读成：

~~~text
GC before: 13 MiB
GC after:   2 MiB
Heap size: 62 MiB usable
~~~

说明这一次 GC 回收了大量短命对象。

### `1.688ms`

这是这次 Stop-The-World pause 的持续时间。

English:

The summary line reports memory before and after the collection, total heap capacity, and pause duration.

## 重点日志 2：Object Age / Survivor

开启：

~~~text
-Xlog:gc+age=trace
~~~

后可能看到：

~~~text
Age table with threshold 2

age 1: 524304 bytes
age 2: 524304 bytes
~~~

中文：

Survivor 中的对象每经历一次 Young GC，年龄会增加。

简化理解：

~~~text
new object
   |
   v
Eden
   |
survives Young GC
   |
   v
Survivor age 1
   |
survives again
   |
   v
Survivor age 2
   |
...
   |
promotion
   v
Old / Tenured
~~~

但要注意：

> Promotion 并不只是“年龄固定到 N 就晋升”。

JVM 还会根据 Survivor 空间压力动态调整 Tenuring Threshold。

因此日志里可能看到：

~~~text
new threshold 1
new threshold 2
new threshold 3
~~~

English:

Object age increases as objects survive young collections. Promotion depends not only on MaxTenuringThreshold but also on survivor-space pressure and JVM heuristics.

## 重点日志 3：Old / Tenured 增长

随着 retained objects 存活，可以观察：

~~~text
Tenured: 2053K -> 2565K
Tenured: 2565K -> 3077K
Tenured: 3077K -> 3589K
~~~

说明：

~~~text
long-lived objects
      |
      v
survive Young GC
      |
      v
promotion
      |
      v
Old / Tenured usage grows
~~~

这正是这个实验最重要的观察之一。

English:

As retained objects survive multiple young collections, Tenured usage rises, showing promotion into the old generation.

## 重点日志 4：Full GC

实验后半段会：

1. 丢掉一半 retained references。
2. 调用 `System.gc()`。
3. 观察 Full GC。

典型日志：

~~~text
GC(10) Pause Full (System.gc())
GC(10) Tenured: 6149K->5126K
GC(10) Pause Full (System.gc()) 8M->5M(62M) 3.632ms
~~~

说明：

~~~text
before Full GC = 8 MiB
after Full GC  = 5 MiB
~~~

由于我们主动释放了一部分长期对象引用，所以 Full GC 后 Heap 明显下降。

## System.gc() 是不是一定 Full GC？ / Does System.gc() Always Mean Full GC?

不能简单说“永远一定”。

`System.gc()` 本质是一个 GC 请求，具体行为与 Collector、JVM 参数和 JVM 实现有关。

例如可以通过：

~~~text
-XX:+DisableExplicitGC
~~~

忽略显式 GC 请求。

本实验使用 SerialGC，是为了得到一个清晰、可重复的教学日志：

~~~text
Pause Full (System.gc())
~~~

English:

System.gc() is a request rather than a universal guarantee. Collector choice and JVM flags affect how explicit GC is handled.

## 为什么短命对象适合 Young Generation / Why Generational GC Works

典型后端请求会产生大量临时对象，例如：

~~~text
HTTP Request
DTO
JSON parsing objects
temporary List / Map
RPC request objects
SQL result wrappers
logging context
~~~

很多对象只活几毫秒。

因此 JVM 可以：

~~~text
frequent cheap Young GC
      +
less frequent expensive old-generation collection
~~~

而不是每次都扫描整个 Heap。

## Memory Pool Snapshot

程序本身还会通过 MXBean 输出 Heap Pool：

~~~text
--- heap pools: before allocation ---

Eden Space
Survivor Space
Tenured Gen
~~~

以及：

~~~text
after young GC pressure
after System.gc()
~~~

让 GC Log 与 Memory Pool 视角互相对应。

## 一个重要误区：Minor GC ≠ 所有 JVM 都这样打印

很多资料写：

~~~text
Minor GC
Major GC
Full GC
~~~

但 JDK 17 Unified Logging 更常看到：

~~~text
Pause Young
Pause Full
~~~

而 G1 又可能出现：

~~~text
Pause Young (Normal) (G1 Evacuation Pause)
Pause Young (Concurrent Start)
Concurrent Mark Cycle
~~~

所以不要只背旧术语，要学会根据 Collector 读实际日志。

## 用 G1 再运行一次 / Compare with G1

理解 SerialGC 后，可以用同样代码换成 G1：

~~~bash
java   -Xms64m   -Xmx64m   -XX:+UseG1GC   -Xlog:gc*,gc+age=trace   -cp target/classes   dev.xbhou.javalab.gc.GcLifecycleDemo
~~~

你会发现：

~~~text
代码完全相同
↓
Collector 不同
↓
GC Region / Event / Log 不同
~~~

English:

Running the same workload with G1 shows that application behavior is the same while collector structures and log events change.

## 如何看 GC 日志 / GC Log Reading Checklist

看到 GC Log 时，不要从头逐字读。

先抓这几个问题：

1. 哪个 Collector？
2. GC 类型是什么？
3. 为什么触发？
4. GC 前多少内存？
5. GC 后多少内存？
6. Pause 多久？
7. Old Generation 是否持续增长？
8. GC Frequency 是否过高？
9. Full GC 是否频繁？
10. GC 后内存是否降不下来？

English:

Start with collector, event type, trigger reason, before/after memory, pause time, old-generation trend, frequency, and post-GC retained memory.

## 后端生产场景怎么判断有问题 / Production Warning Signs

常见危险信号：

~~~text
Young GC too frequent
+
pause time rising
~~~

或者：

~~~text
Old Gen keeps growing
+
Full GC
+
memory does not fall after GC
~~~

可能意味着：

- Allocation Rate 太高
- Cache 太大
- Memory Leak
- Old Generation 压力
- Heap 配置不合理
- 对象生命周期异常
- 大对象过多

## GC 与 Memory Leak 的关系 / GC vs Memory Leak

GC 能回收的是：

> 不再可达的对象。

如果代码一直持有引用，例如 static cache，即使业务已经不需要这些对象，只要它们仍然 reachable，GC 就不能回收。

所以：

> Java 有 GC，不代表 Java 不会发生 Memory Leak。

English:

Garbage collectors reclaim unreachable objects. A Java memory leak happens when objects remain reachable even though the application no longer needs them.

## 与上一个 Lab 的关系 / Relationship to Memory Regions Lab

~~~text
Heap / Stack / Metaspace
        |
        v
Where memory lives
        |
        v
GC Observation
        |
        v
How heap memory is reclaimed
~~~

上一个 Lab 回答：

> 内存在哪？

这个 Lab 回答：

> Heap 里的对象什么时候、如何被回收？

## 生产与面试问题 / Production & Interview Questions

1. Young GC 为什么通常比 Full GC 便宜？
2. Eden 满了为什么会触发 Young GC？
3. Survivor 是做什么的？
4. Object Age 是什么？
5. MaxTenuringThreshold 是不是固定晋升年龄？
6. 什么是 Promotion？
7. 为什么 Old Gen 持续增长需要警惕？
8. `13M->2M(62M)` 怎么读？
9. Full GC 后内存仍然很高意味着什么？
10. Java 有 GC 为什么仍然会 Memory Leak？
11. G1 和 SerialGC 的日志为什么不同？
12. GC Pause 为什么会影响 p99 latency？

## 下一步 / Next Step

Phase 2 下一步进入：

> Thread Dump Analysis / 线程转储分析

重点会实际制造并观察：

- BLOCKED
- WAITING
- TIMED_WAITING
- Deadlock
- Thread Pool Saturation
- `jstack` / `jcmd Thread.print`

把 JVM 故障排查从“看 GC”继续推进到“看线程”。
