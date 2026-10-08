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

后续覆盖：

- Heap / Stack / Metaspace
- GC 行为
- Thread Dump
- JVM 诊断与性能分析

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

Planned next topics:

- heap / stack / metaspace
- garbage collection
- thread dumps
- JVM diagnostics and performance behavior
