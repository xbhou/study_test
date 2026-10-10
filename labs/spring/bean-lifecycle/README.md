# Spring Bean 生命周期 / Spring Bean Lifecycle

## 目标 / Goal

中文：

这个 Lab 把一个 Spring Bean 从“定义”到“销毁”的关键生命周期阶段实际打印出来。

重点观察：

- Bean 实例化 / Instantiation
- Dependency Injection
- Aware 回调
- BeanPostProcessor before initialization
- @PostConstruct
- InitializingBean.afterPropertiesSet
- custom initMethod
- BeanPostProcessor after initialization
- Bean Ready
- @PreDestroy
- DisposableBean.destroy
- custom destroyMethod

English:

This lab makes the important Spring Bean lifecycle stages observable from instantiation through destruction.

## 为什么重写旧 studyDemo / Why Rewrite the Legacy studyDemo

旧项目中已经有：

~~~text
ApplicationContextHelloWorld
BeanFactoryHelloWorld
InitializingBeanPerson
spring-config.xml
~~~

其中 `InitializingBeanPerson` 同时实现了：

~~~text
InitializingBean
BeanFactoryAware
BeanPostProcessor
~~~

这是很有价值的历史实验，但几个概念混在同一个类里，不容易看清：

> 哪个角色是“被管理的 Bean”，哪个角色是“处理其他 Bean 的容器扩展点”。

新 Lab 把角色拆开：

~~~text
LifecycleBean
-> 被 Spring 管理的业务 Bean

TrackingBeanPostProcessor
-> 容器扩展点，观察 Bean 初始化前后

AppConfig
-> BeanDefinition 来源

AnnotationConfigApplicationContext
-> 容器
~~~

English:

The legacy demo mixed the managed bean and the BeanPostProcessor role in one class. This lab separates those responsibilities so lifecycle callbacks are easier to understand.

## 生命周期总览 / Lifecycle Overview

简化后的主链路：

~~~text
BeanDefinition
     |
     v
Instantiate Bean
     |
     v
Populate Properties / Dependency Injection
     |
     v
Aware callbacks
     |
     v
BeanPostProcessor.beforeInitialization
     |
     v
@PostConstruct
     |
     v
InitializingBean.afterPropertiesSet
     |
     v
custom initMethod
     |
     v
BeanPostProcessor.afterInitialization
     |
     v
Ready Bean
     |
     v
ApplicationContext.close()
     |
     v
@PreDestroy
     |
     v
DisposableBean.destroy
     |
     v
custom destroyMethod
~~~

注意：

> 这是一条适合建立心智模型的主链路。真实 Spring 中有多个 BeanPostProcessor，某些框架功能会在这些扩展点里加入代理、注入或其他处理。

English:

This is a practical mental model. Real Spring applications can have many BeanPostProcessors that add dependency injection, proxies, annotation processing, and framework-specific behavior.

## 实际输出 / Expected Output

运行 Lab 后，核心 Bean 会按顺序记录：

~~~text
01 constructor
02 dependency injection: setDependency
03 BeanNameAware.setBeanName
04 BeanFactoryAware.setBeanFactory
05 BeanPostProcessor.beforeInitialization
06 @PostConstruct
07 InitializingBean.afterPropertiesSet
08 custom initMethod
09 BeanPostProcessor.afterInitialization
10 business method
11 @PreDestroy
12 DisposableBean.destroy
13 custom destroyMethod
~~~

程序最后会自动校验顺序，如果生命周期顺序与预期不一致，会直接抛出异常。

English:

The demo records and verifies the lifecycle order at runtime instead of relying only on printed notes.

## 1. Instantiation / 实例化

Spring 首先需要创建对象：

~~~java
public LifecycleBean(LifecycleEventRecorder recorder) {
    ...
}
~~~

这一步本质仍然是 Java 对象创建。

但在 Spring 中，对象创建受到 BeanDefinition 和容器生命周期管理。

可以建立这样的关系：

~~~text
Class
  |
BeanDefinition
  |
Spring decides how to instantiate
  |
Java object
~~~

## 2. Dependency Injection / 依赖注入

本 Lab 用 setter injection 明确观察“实例化”和“注入”不是同一步：

~~~java
@Autowired
public void setDependency(Dependency dependency) {
    ...
}
~~~

顺序是：

~~~text
constructor
    |
    v
setter injection
~~~

这很重要，因为：

> Bean 已经被 new 出来，不代表它的依赖已经全部准备完成。

English:

Instantiation and dependency injection are separate phases. An object can already exist before all of its Spring-managed dependencies have been populated.

## 3. Aware 回调 / Aware Callbacks

LifecycleBean 实现：

~~~text
BeanNameAware
BeanFactoryAware
~~~

因此 Spring 会告诉 Bean：

~~~text
你的 beanName 是什么
你的 BeanFactory 是谁
~~~

这类接口让 Bean 获得容器基础设施信息。

常见 Aware：

- BeanNameAware
- BeanFactoryAware
- ApplicationContextAware
- EnvironmentAware
- ResourceLoaderAware

生产代码不要因为“方便”就大量实现 Aware，否则业务代码会更强地耦合 Spring。

English:

Aware interfaces expose container infrastructure to the bean. They are useful but increase coupling between business code and Spring.

## 4. BeanPostProcessor 是什么 / What Is BeanPostProcessor?

BeanPostProcessor 不是某一个业务 Bean 的生命周期接口，而是：

> Spring 容器级扩展点，用来处理很多 Bean。

核心方法：

~~~java
postProcessBeforeInitialization(...)
postProcessAfterInitialization(...)
~~~

这也是很多 Spring 魔法的重要基础。

例如 Spring 中大量能力都与 BeanPostProcessor 思想相关：

- @Autowired
- @PostConstruct
- AOP proxy
- @Async
- @Transactional

具体实现可能由不同 Processor 负责。

English:

BeanPostProcessor is a container extension point that can inspect or replace many beans before and after initialization.

## 5. @PostConstruct

~~~java
@PostConstruct
public void postConstruct() {
    ...
}
~~~

适合表达：

> 依赖已经注入完成后，我需要做初始化工作。

它发生在常规 initialization callback 阶段中。

## 6. InitializingBean.afterPropertiesSet

~~~java
implements InitializingBean
~~~

然后：

~~~java
afterPropertiesSet()
~~~

这是 Spring API 风格的初始化回调。

优点：

- 明确
- Spring 原生

缺点：

- 业务类直接依赖 Spring API

所以普通业务 Bean 往往更倾向：

- @PostConstruct
- 配置 initMethod

而不是为了一个初始化动作专门实现 InitializingBean。

## 7. custom initMethod

配置：

~~~java
@Bean(initMethod = "customInit")
~~~

Bean 本身不需要实现 Spring 生命周期接口。

因此：

~~~text
@PostConstruct
InitializingBean
custom initMethod
~~~

虽然都属于初始化，但耦合方式不同。

本 Lab 故意同时使用它们，是为了观察顺序；真实业务通常没有必要三种一起上。

English:

The lab deliberately combines three initialization mechanisms for teaching. Production beans normally choose the simplest appropriate mechanism rather than using all three.

## 8. BeanPostProcessor.afterInitialization

初始化完成后，Spring 再执行 after-initialization processors。

这里特别重要，因为：

> AOP Proxy 往往就是在 BeanPostProcessor 阶段产生或包装的。

因此：

~~~text
original object
     |
BeanPostProcessor
     |
possibly wrapped / proxied
     |
object returned by getBean()
~~~

这为下一个 AOP Lab 做准备。

## 9. 销毁生命周期 / Destruction Lifecycle

当：

~~~java
context.close();
~~~

Spring 开始销毁 singleton beans。

本 Lab 可以观察：

~~~text
@PreDestroy
DisposableBean.destroy
custom destroyMethod
~~~

和初始化一样，它们代表三种不同耦合方式。

注意：

> Prototype Bean 的完整销毁生命周期通常不由容器自动管理，这和 Singleton 不同。

## BeanFactory vs ApplicationContext

旧 studyDemo 同时有：

~~~text
BeanFactoryHelloWorld
ApplicationContextHelloWorld
~~~

这是一个很好的问题。

本 Lab 额外提供：

~~~text
BeanFactoryVsApplicationContextDemo
~~~

用于观察：

### Bare BeanFactory

~~~text
register BeanDefinition
     |
没有创建 singleton
     |
getBean()
     |
才创建 Bean
~~~

### ApplicationContext

~~~text
register Bean
     |
refresh()
     |
默认 pre-instantiate non-lazy singletons
~~~

所以可以先这样记：

> BeanFactory 是底层 Bean 容器能力；ApplicationContext 在其上提供更完整的应用上下文生命周期和基础设施。

English:

A bare BeanFactory can instantiate on demand, while ApplicationContext refresh normally pre-instantiates non-lazy singleton beans and adds richer application infrastructure.

## 运行 / Run

需要 / Requires:

- JDK 17+
- Maven

~~~bash
mvn clean compile
~~~

完整生命周期：

~~~bash
java   -cp "target/classes:$(cat cp.txt 2>/dev/null)"   dev.xbhou.javalab.spring.lifecycle.App
~~~

更方便的 Maven 运行方式：

~~~bash
mvn   org.codehaus.mojo:exec-maven-plugin:3.5.0:java   -Dexec.mainClass=dev.xbhou.javalab.spring.lifecycle.App
~~~

BeanFactory vs ApplicationContext：

~~~bash
mvn   org.codehaus.mojo:exec-maven-plugin:3.5.0:java   -Dexec.mainClass=dev.xbhou.javalab.spring.lifecycle.BeanFactoryVsApplicationContextDemo
~~~

## 与 JVM Class Loading 的关系 / Relationship to JVM Class Loading

前面 JVM Lab 研究：

~~~text
Class Loading
Initialization
Reflection
~~~

Spring 在这个基础上进一步构建：

~~~text
Class metadata
     |
BeanDefinition
     |
Reflection / constructor invocation
     |
Dependency Injection
     |
Lifecycle callbacks
     |
Proxy / enhancement
~~~

所以 Spring 并不是绕开 JVM，而是在 JVM 机制上增加了一层 IoC Container。

## 常见误区 / Common Misconceptions

### 1. constructor 执行完，Bean 就 Ready 了

错误。

~~~text
constructor
!=
fully initialized Spring Bean
~~~

后面还有：

- dependency injection
- aware callbacks
- BeanPostProcessor
- init callbacks

### 2. @PostConstruct 在构造器之前

错误。

对象必须先存在，才能执行实例方法。

### 3. BeanPostProcessor 只处理一个 Bean

错误。

它是容器扩展点，可以处理很多 Bean。

### 4. InitializingBean 和 @PostConstruct 必须同时使用

错误。

本 Lab 同时使用只是为了教学。

### 5. getBean() 拿到的一定是原始对象

不一定。

如果 AOP 等 BeanPostProcessor 创建了 Proxy：

~~~text
getBean()
-> proxy
-> target
~~~

这就是下一个 AOP Lab 的重点。

## 生产与面试问题 / Production & Interview Questions

1. BeanDefinition 和 Bean instance 有什么区别？
2. Bean 的实例化和依赖注入是不是同一步？
3. Aware callback 在什么时候发生？
4. BeanPostProcessor 和 BeanFactoryPostProcessor 有什么区别？
5. @PostConstruct、InitializingBean、initMethod 顺序是什么？
6. 为什么 @Transactional 与 BeanPostProcessor / Proxy 有关系？
7. ApplicationContext refresh 做了什么？
8. BeanFactory 与 ApplicationContext 有什么关系？
9. Singleton Bean 什么时候实例化？
10. Prototype Bean 的销毁谁负责？
11. 为什么构造器中不适合依赖“所有 Spring 生命周期已经完成”？
12. getBean() 为什么可能拿到 Proxy 而不是原始对象？

## 下一步 / Next Step

Phase 3 下一步进入：

> AOP

重点会实际观察：

~~~text
Target Bean
   |
BeanPostProcessor
   |
Proxy
   |
Method Interceptor
   |
Target Method
~~~

并比较：

- JDK Dynamic Proxy
- CGLIB-style class proxy
- self-invocation
- @Transactional 为什么会有“类内调用失效”问题
