# Legacy Code Migration Guide

The repository contains useful historical experiments. They should not all be moved mechanically.

The migration rule is:

> Revisit a topic -> define a concrete question -> rewrite or refactor the smallest useful experiment -> place it under `labs/`.

## Existing `src/` Mapping

| Existing area | Future lab area | Action |
|---|---|---|
| `algorithm/sort` | `labs/java-core/` or a future algorithm lab | Keep until revisited |
| `algorithm/dp` | future algorithm lab | Keep until revisited |
| `jdk/reflect` | `labs/java-core/reflection` | Rewrite when reviewing reflection |
| `jdk/loadClass` | `labs/jvm/class-loading` | High-value migration |
| `jdk/single` | `labs/java-core/design-patterns` | Keep selected examples |
| `jdk/thread/synch` | `labs/concurrency` | Rewrite as focused experiments |
| `jdk/threadPool` | `labs/concurrency/thread-pool` | High-value migration |
| JavaScript engine examples | legacy only | Keep for history unless needed again |
| one-off data-cleaning utilities | legacy only | Do not promote unless generally useful |

## `studyDemo` Mapping

The private `studyDemo` repository contains several useful Spring/MyBatis learning experiments.

Recommended future destinations:

- Spring `ApplicationContext` / `BeanFactory`
  - `labs/spring/bean-container`
- Bean lifecycle / `InitializingBean` / post processors
  - `labs/spring/bean-lifecycle`
- Spring AOP
  - `labs/spring/aop`
- Custom `PropertyEditor`
  - `labs/spring/property-binding`
- MyBatis bootstrap
  - future persistence/MyBatis lab

Do not copy these examples unchanged. When a topic is revisited, recreate the experiment using a current JDK and a clear README explaining the question and observations.

## Priority

1. Concurrency and CompletableFuture
2. Thread pool behavior
3. JVM class loading
4. Spring bean lifecycle
5. Transactions
6. Kafka / Redis / MySQL / gRPC
7. Distributed-system patterns

This order favors backend engineering and system-design skills over preserving every historical snippet.
