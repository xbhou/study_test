# JVM 类加载生命周期 / JVM Class Loading Lifecycle

## 问题 / Question

中文：

“类被 JVM 看见了”和“类的 static 代码已经执行了”不是一回事。

本 Lab 重点回答：

- Loading、Linking、Initialization 分别是什么？
- 访问编译期常量为什么可能不触发初始化？
- `ClassLoader.loadClass` 会不会执行 static block？
- `Class.forName(..., false, ...)` 和默认 `Class.forName` 有什么区别？
- 通过子类访问父类 static 字段时，谁会初始化？
- 子类初始化之前为什么父类必须先初始化？

English:

A class being loaded by the JVM does not mean its static initialization has already executed.

This lab explores loading, linking, initialization, compile-time constants, `ClassLoader.loadClass`, `Class.forName`, and parent/child initialization behavior.

## 生命周期 / Lifecycle

简化理解：

~~~text
Loading
   |
   v
Linking
   ├─ Verification
   ├─ Preparation
   └─ Resolution
   |
   v
Initialization
~~~

中文：

- Loading：找到 class bytes，并创建对应的 `Class` 对象。
- Verification：验证字节码结构和类型安全。
- Preparation：为 static 字段分配内存并设置 JVM 默认值。
- Resolution：把符号引用转换为直接引用；实际 JVM 中部分解析可以延迟发生。
- Initialization：执行类初始化逻辑，包括 static 字段显式赋值和 static block。

English:

- Loading finds class bytes and creates the runtime class representation.
- Verification checks bytecode correctness and safety.
- Preparation allocates static storage and assigns JVM default values.
- Resolution turns symbolic references into direct references; some resolution may be lazy.
- Initialization executes explicit static field initialization and static blocks.

## 场景 1：编译期常量不触发初始化 / Compile-time Constant Does Not Initialize

Target:

~~~java
public static final String VALUE = "CONST";
~~~

访问：

~~~java
System.out.println(CompileTimeConstantTarget.VALUE);
~~~

通常不会触发：

~~~text
CompileTimeConstantTarget initialized
~~~

原因是编译期常量可能直接被内联进调用方字节码。

中文：

这不是“static final 永远不初始化类”，而是“满足编译期常量条件的值可能被内联”。

English:

This does not mean every `static final` field avoids initialization. The key distinction is whether it is a compile-time constant that can be inlined.

## 场景 2：普通 static 字段触发初始化 / Non-constant Static Field Triggers Initialization

~~~java
public static int VALUE = 42;
~~~

第一次主动读取：

~~~java
RuntimeStaticFieldTarget.VALUE
~~~

会触发该类初始化，因此 static block 会先执行。

## 场景 3：loadClass 只加载，不初始化 / loadClass Loads Without Initialization

~~~java
ClassLoader loader = App.class.getClassLoader();

Class<?> type = loader.loadClass(
    "dev.xbhou.javalab.classloading.targets.LoadOnlyTarget"
);
~~~

此时类可以已经被 JVM 加载，但 static block 还没有执行。

随后：

~~~java
Class.forName(type.getName(), true, loader);
~~~

才显式触发初始化。

核心区别：

~~~text
loaded
!=
initialized
~~~

## 场景 4：Class.forName initialize=false / Class.forName Without Initialization

~~~java
Class.forName(name, false, loader);
~~~

允许加载类，但不执行初始化。

之后：

~~~java
Class.forName(name, true, loader);
~~~

才触发 static 初始化逻辑。

默认的：

~~~java
Class.forName(name)
~~~

会执行初始化。

## 场景 5：通过子类访问父类字段 / Accessing Parent Static Field Through Child

~~~java
ChildTarget.PARENT_VALUE
~~~

虽然语法写的是 ChildTarget，但字段实际声明在 ParentTarget。

结果：

~~~text
ParentTarget initialized
ChildTarget not initialized yet
~~~

这说明初始化由“真正声明 static 成员的类”决定，而不是简单看源码里写了哪个类型名。

## 场景 6：初始化子类时先初始化父类 / Parent Initializes Before Child

随后第一次主动使用：

~~~java
ChildTarget.CHILD_VALUE
~~~

如果 ChildTarget 尚未初始化，则 JVM 必须先保证父类已经初始化。

本 Lab 前一步已经初始化了 ParentTarget，所以这里只会看到 ChildTarget 的初始化输出。

如果在独立 JVM 中直接初始化 ChildTarget，则顺序是：

~~~text
ParentTarget initialized
ChildTarget initialized
~~~

## 运行 / Run

需要 / Requires JDK 17+ and Maven.

~~~bash
mvn clean compile
java -cp target/classes dev.xbhou.javalab.classloading.App
~~~

## 用 JVM 日志观察真实加载 / Observe with JVM Class Logs

JDK 17 可以进一步开启 unified logging：

~~~bash
java   -Xlog:class+load=info,class+init=info   -cp target/classes   dev.xbhou.javalab.classloading.App
~~~

输出会很多，可以只筛选本 Lab：

~~~bash
java   -Xlog:class+load=info,class+init=info   -cp target/classes   dev.xbhou.javalab.classloading.App 2>&1   | grep xbhou
~~~

这样可以把：

~~~text
JVM load log
+
我们的 static initialization print
~~~

放在一起理解。

## 与旧代码的关系 / Relationship to Legacy Code

仓库里保留着 2018 年的实验：

~~~text
src/main/java/cn/study/jdk/loadClass/StaticTest.java
src/main/java/cn/study/jdk/j2se/js/loadClass/MyClassLoader.java
~~~

旧 `StaticTest` 已经在观察 static 字段、实例初始化块、构造器和 static block 的顺序。

这个新 Lab 没有删除旧代码，而是把问题升级为：

~~~text
历史观察
   ↓
明确区分 Loading / Linking / Initialization
   ↓
用 JDK 17 API 做可重复实验
   ↓
加入 JVM class log
   ↓
形成可解释的 JVM mental model
~~~

English:

The old experiments are preserved as historical learning assets. This lab reframes them around explicit JVM lifecycle questions and modern JDK observability.

## 架构意义 / Architecture Insight

理解 Class Loading 对 Java 后端并不是纯面试题。

它会直接帮助理解：

- Spring Bean / Framework class scanning
- SPI / ServiceLoader
- JDBC Driver loading
- Plugin architecture
- Hot deployment
- Application Server classloader isolation
- Java Agent
- Reflection
- Dynamic proxy
- Multiple versions of the same dependency

尤其重要的一点：

~~~text
same class name
+
different ClassLoader
=
different runtime type identity
~~~

后续可以单独做 Custom ClassLoader / Parent Delegation 实验。

## 生产与面试问题 / Production & Interview Questions

1. Loading 和 Initialization 有什么区别？
2. Preparation 阶段 static int 会得到什么值？
3. 哪些行为属于主动使用并触发初始化？
4. 编译期常量为什么可能不触发初始化？
5. `ClassLoader.loadClass` 和 `Class.forName` 有什么区别？
6. 子类初始化前为什么父类要先初始化？
7. 为什么两个 ClassLoader 加载的同名类可能不能互相 cast？
8. Parent Delegation 解决了什么问题？
9. Spring Boot / Tomcat 为什么会涉及 ClassLoader？
10. 什么情况下需要自定义 ClassLoader？

## 下一步 / Next Step

Phase 2 接下来建议进入：

> Heap / Stack / Metaspace

重点用代码和 JDK 工具观察：

- 对象在 Heap
- 方法调用与 Stack Frame
- static metadata 与 Metaspace
- StackOverflowError
- OutOfMemoryError 的不同类型
