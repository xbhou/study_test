# Java Lab

A personal Java engineering lab for small, runnable experiments and backend system design practice.

## What this repository is for

This repository turns technical questions into executable experiments.

The goal is not to collect snippets. Each new lab should answer a concrete question, be easy to run, and document what was learned.

## Repository Structure

```text
java-lab
├── src/                         # Legacy Java study experiments
├── labs/                        # New standalone experiments
│   ├── java-core/
│   ├── concurrency/
│   ├── jvm/
│   ├── spring/
│   ├── mysql/
│   ├── redis/
│   ├── kafka/
│   ├── grpc/
│   └── distributed-system/
└── docs/
    └── ROADMAP.md
```

## Modern Labs

### CompletableFuture Quote Aggregator

```text
labs/concurrency/completable-future-quote
```

Demonstrates concurrent provider requests, timeout isolation, result aggregation, and best-quote selection.

### ThreadPoolExecutor Behavior

```text
labs/concurrency/thread-pool-behavior
```

Demonstrates core threads, bounded queues, maximum pool growth, saturation, and rejection behavior.

## Existing Topics

The legacy `src/` tree contains experiments covering:

- algorithms
- reflection
- class loading
- singleton patterns
- synchronized / volatile
- thread pools
- date and time APIs
- JavaScript engine experiments

These examples are kept as historical learning assets and will be cleaned up gradually instead of being moved all at once.

## Lab Rules

1. One lab, one concrete technical question.
2. Prefer runnable examples over copied notes.
3. Keep each lab small enough to understand independently.
4. Include a short README with problem, design, run steps, and observations.
5. Add tests when behavior is important.
6. Never commit company code, credentials, internal endpoints, or production data.

## Roadmap

See [docs/ROADMAP.md](docs/ROADMAP.md).
