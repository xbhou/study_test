# JVM

## 中文

JVM 实验目录，重点通过可运行代码和 JDK 工具理解运行时行为。

### Labs

#### 类加载生命周期 / Class Loading Lifecycle

路径：`class-loading-lifecycle/`

重点：

- Loading / Linking / Initialization
- Verification / Preparation / Resolution
- 编译期常量与类初始化
- `ClassLoader.loadClass`
- `Class.forName(..., false, ...)`
- 父类 / 子类初始化顺序
- JDK 17 `-Xlog:class+load,class+init`

#### JVM 内存区域 / JVM Memory Regions

路径：`memory-regions/`

重点：

- Heap / Thread Stack / Metaspace
- `-Xss`
- `-Xms` / `-Xmx`
- `-XX:MaxMetaspaceSize`
- `StackOverflowError`
- `OutOfMemoryError: Java heap space`
- `OutOfMemoryError: Metaspace`
- ClassLoader Leak 与进程总内存

#### GC 观察 / GC Observation

路径：`gc-observation/`

重点：

- Young GC
- Eden / Survivor / Tenured
- Object Age / Tenuring Threshold
- Promotion
- Full GC
- JDK Unified Logging `-Xlog:gc*`
- SerialGC 与 G1 日志对比

#### 线程转储分析 / Thread Dump Analysis

路径：`thread-dump-analysis/`

重点：

- RUNNABLE / BLOCKED / WAITING / TIMED_WAITING
- Monitor Owner / Waiter
- Java-level Deadlock
- Thread Pool Saturation
- `jcmd Thread.print -l`
- `jstack -l`
- 连续多份 Thread Dump 对比
- JVM 线上线程故障排查

Phase 2 基础 JVM 路线到这里完成。

---

## English

JVM experiments focused on observable runtime behavior through runnable code and JDK tooling.

### Labs

#### Class Loading Lifecycle / 类加载生命周期

Path: `class-loading-lifecycle/`

Focus:

- loading / linking / initialization
- verification / preparation / resolution
- compile-time constants and initialization
- `ClassLoader.loadClass`
- `Class.forName(..., false, ...)`
- parent / child initialization order
- JDK 17 `-Xlog:class+load,class+init`

#### JVM Memory Regions / JVM 内存区域

Path: `memory-regions/`

Focus:

- heap / thread stack / metaspace
- `-Xss`
- `-Xms` / `-Xmx`
- `-XX:MaxMetaspaceSize`
- `StackOverflowError`
- `OutOfMemoryError: Java heap space`
- `OutOfMemoryError: Metaspace`
- class-loader leaks and total process memory

#### GC Observation / GC 观察

Path: `gc-observation/`

Focus:

- young collections
- Eden / Survivor / Tenured
- object age / tenuring threshold
- promotion
- full collections
- JDK unified logging with `-Xlog:gc*`
- SerialGC vs G1 log comparison

#### Thread Dump Analysis / 线程转储分析

Path: `thread-dump-analysis/`

Focus:

- RUNNABLE / BLOCKED / WAITING / TIMED_WAITING
- monitor ownership and waiters
- Java-level deadlocks
- thread-pool saturation
- `jcmd Thread.print -l`
- `jstack -l`
- comparing multiple thread dumps
- practical JVM thread troubleshooting

The foundational Phase 2 JVM track is complete.
