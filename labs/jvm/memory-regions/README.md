# JVM 内存区域实验 / JVM Memory Regions Lab

## 目标 / Goal

中文：

这个 Lab 不靠死背 JVM 内存图，而是用几个独立进程观察：

- Heap
- Thread Stack
- Metaspace

以及三种完全不同的失败：

- `StackOverflowError`
- `OutOfMemoryError: Java heap space`
- `OutOfMemoryError: Metaspace`

English:

This lab uses isolated JVM processes to observe Heap, Thread Stack, and Metaspace, and to distinguish three different failures: stack overflow, heap exhaustion, and metaspace exhaustion.

## 心智模型 / Mental Model

~~~text
JVM Process
|
+-- Heap
|   +-- Java objects
|   +-- arrays
|   +-- shared by threads
|
+-- Thread Stack (one per thread)
|   +-- stack frames
|   +-- local variables / references
|   +-- method call state
|
+-- Metaspace
    +-- class metadata
    +-- method metadata
    +-- runtime constant-pool related metadata
    +-- native memory, not ordinary Java Heap
~~~

中文：

需要特别注意：

- “对象引用”可以位于 Stack Frame 中，但它指向的对象通常在 Heap。
- Metaspace 存的是类元数据，不等于 static 字段对象本身都放在 Metaspace。
- 每个线程都有自己的 Java Stack；Heap 是多个线程共享的。
- JDK 8 以后 HotSpot 用 Metaspace 替代了永久代（PermGen）。

English:

A local reference may live in a stack frame while the referenced object lives on the heap. Metaspace stores class metadata; it should not be simplified into “all static data lives in metaspace.” Each thread owns its Java stack, while the heap is shared.

## 场景 1：观察 Heap 与 Metaspace / Observe Heap and Metaspace

运行：

~~~bash
mvn clean compile
java -cp target/classes dev.xbhou.javalab.memory.MemoryOverview
~~~

程序会通过 JMX / MXBean 输出：

- Heap used / committed / max
- Metaspace used / committed
- 当前线程 Stack Trace 深度
- 已加载 Class 数量

中文：

这是“正常运行状态”的基线实验。JVM 并没有公开一个 API 让你直接打印“这个 local variable 在 Stack 的地址”，因此我们通过 Thread Stack、Heap MXBean 和 Metaspace MXBean 分别观察这些区域。

English:

This is the normal baseline. Java does not expose a simple API that prints the physical stack address of a local variable, so the lab observes the regions through stack traces and JVM management beans.

## 场景 2：StackOverflowError / Stack Exhaustion

代码不断递归：

~~~text
method()
  -> method()
      -> method()
          -> ...
~~~

建议使用一个较小但受控的 Thread Stack：

~~~bash
java   -Xss256k   -cp target/classes   dev.xbhou.javalab.memory.StackOverflowDemo
~~~

预期：

~~~text
StackOverflowError
approximate recursion depth = ...
~~~

核心：

> StackOverflowError 通常是“单个线程的调用栈空间不够”，不是 Heap 不够。

English:

StackOverflowError is typically about one thread exhausting its stack through excessive call depth. It is not the same problem as heap exhaustion.

## 场景 3：Java Heap OOM / Heap Exhaustion

程序不断创建约 1 MiB 的 byte array，并保留引用：

~~~text
List<byte[]>
  |
  +-- 1 MiB
  +-- 1 MiB
  +-- 1 MiB
  +-- ...
~~~

必须使用受控 Heap：

~~~bash
java   -Xms32m   -Xmx32m   -cp target/classes   dev.xbhou.javalab.memory.HeapOomDemo
~~~

预期进程最终退出并出现：

~~~text
java.lang.OutOfMemoryError: Java heap space
~~~

中文：

这不是“对象太大”这一种情况。Heap OOM 的本质是 JVM 无法再满足新的 Heap 分配请求，即使 GC 后也无法得到足够可用空间。

English:

Heap OOM means the JVM cannot satisfy a new heap allocation even after garbage collection attempts to reclaim memory.

## 场景 4：Metaspace OOM / Metaspace Exhaustion

这个实验持续创建：

~~~text
new ClassLoader
      +
new generated Proxy class
      +
retain ClassLoader / Class reference
~~~

为什么需要不同 ClassLoader？

~~~text
same proxy shape
+
different ClassLoader
=
different runtime Class
~~~

只要 ClassLoader 仍被强引用，这些 Class 通常就无法卸载，对应的类元数据会继续占用 Metaspace。

必须限制 Metaspace：

~~~bash
java   -Xmx256m   -XX:MaxMetaspaceSize=32m   -cp target/classes   dev.xbhou.javalab.memory.MetaspaceOomDemo
~~~

预期：

~~~text
generated proxy classes = 500
generated proxy classes = 1000
...
java.lang.OutOfMemoryError: Metaspace
~~~

English:

The demo creates a new class loader and a generated proxy class repeatedly, retaining both. Because the class loaders stay reachable, their classes cannot normally be unloaded, so metaspace usage keeps growing.

## 为什么 Metaspace 不直接放在 Java Heap / Why Metaspace Is Different

HotSpot 的 Metaspace 使用 native memory。

因此：

~~~text
-Xmx
-> primarily limits Java Heap

-XX:MaxMetaspaceSize
-> limits Metaspace
~~~

两者是不同的容量边界。

这也是为什么下面两种错误含义完全不同：

~~~text
OutOfMemoryError: Java heap space
OutOfMemoryError: Metaspace
~~~

## 观察 JVM Memory Pool / Observe Memory Pools

可以查看 JVM 暴露的 Memory Pool 名称：

~~~bash
java   -Xlog:gc*=info   -cp target/classes   dev.xbhou.javalab.memory.MemoryOverview
~~~

也可以使用：

~~~bash
jcmd <pid> GC.heap_info
jcmd <pid> VM.native_memory summary
~~~

注意：

> `VM.native_memory` 需要 JVM 启动时开启 Native Memory Tracking。

例如：

~~~bash
java   -XX:NativeMemoryTracking=summary   -cp target/classes   dev.xbhou.javalab.memory.MemoryOverview
~~~

然后：

~~~bash
jcmd <pid> VM.native_memory summary
~~~

## Stack、Heap、Metaspace 对比 / Comparison

| 区域 / Region | 主要内容 / Main Content | 常见容量参数 / Typical Limit | 典型错误 / Typical Failure |
| --- | --- | --- | --- |
| Thread Stack | Stack Frame、局部变量、调用状态 | `-Xss` | `StackOverflowError` |
| Heap | Object、Array | `-Xms`, `-Xmx` | `OutOfMemoryError: Java heap space` |
| Metaspace | Class Metadata | `-XX:MaxMetaspaceSize` | `OutOfMemoryError: Metaspace` |

## 不要混淆的几个概念 / Common Misconceptions

### 1. static 不等于“全部放 Metaspace”

中文：

类元数据位于 Metaspace，但 static 字段如果引用一个 Java Object，对象本身仍然通常位于 Heap。

English:

Class metadata is stored in metaspace, but a Java object referenced by a static field still normally lives on the heap.

### 2. Stack 不存“所有基本类型”

变量放在哪里取决于上下文。

例如对象字段中的 `int` 是对象布局的一部分，因此随对象一起在 Heap。

### 3. OOM 不只有一种

~~~text
Java heap space
Metaspace
Direct buffer memory
Unable to create native thread
GC overhead limit exceeded
...
~~~

`OutOfMemoryError` 是一个错误家族，不是单一原因。

## 为什么对后端工程有用 / Backend Engineering Relevance

这些知识会直接关联到：

- 容器内存限制
- Kubernetes OOMKilled
- `-Xmx` 该配多少
- Thread 数量过多导致 native memory 压力
- 动态代理 / CGLIB / ByteBuddy 大量生成 Class
- ClassLoader Leak
- 热部署
- Spring / Tomcat ClassLoader
- Heap Dump
- GC 调优

一个后端进程的总内存并不等于：

~~~text
-Xmx
~~~

更接近：

~~~text
Process Memory
=
Heap
+ Metaspace
+ Thread Stacks
+ Direct Memory
+ Code Cache
+ GC structures
+ JVM native memory
+ native libraries
+ ...
~~~

这对容器容量设计非常重要。

## 安全运行原则 / Safe Execution

这些压力实验应该：

1. 单独启动 JVM。
2. 显式设置较小的容量限制。
3. 不要在生产环境运行。
4. 不要把 Metaspace Demo 去掉限制后无限运行。
5. 观察错误后让进程退出即可。

English:

Run pressure demos only in isolated JVMs with explicit small limits. Do not execute them in production or remove the resource caps and let them grow without bounds.

## 生产与面试问题 / Production & Interview Questions

1. Heap 和 Thread Stack 的生命周期有什么区别？
2. 为什么一个线程 StackOverflow 不代表 Heap 满了？
3. `-Xmx` 是否等于 Java 进程最大内存？
4. Metaspace 存什么？
5. JDK 8 为什么移除了 PermGen？
6. 为什么 ClassLoader Leak 会导致 Metaspace 增长？
7. static Object 到底在哪里？
8. Thread 数量为什么影响 native memory？
9. 容器 memory limit 和 `-Xmx` 应如何留余量？
10. Heap OOM、Metaspace OOM、native thread OOM 如何区分？

## 下一步 / Next Step

Phase 2 下一步进入：

> GC Observation / GC 观察实验

会用受控对象分配配合：

- `-Xlog:gc`
- Young GC
- Full GC
- Allocation Rate
- Survivor / Promotion

把“GC 理论”转成可以观察的 JVM 行为。
