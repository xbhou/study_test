# CompletableFuture Quote Aggregator

## Question

How can a service request several independent providers concurrently, ignore slow or failed providers, and return the best valid quote?

## Design

```text
Quote Request
     |
     v
+-------------------+
| QuoteAggregator   |
+-------------------+
   |      |      |
   v      v      v
Provider A B      C
   \      |      /
    \-- concurrent --/
            |
            v
      timeout / errors
            |
            v
     valid quote list
            |
            v
       best quote
```

## What this lab demonstrates

- `CompletableFuture.supplyAsync`
- a dedicated executor
- `orTimeout`
- exception isolation
- `allOf`
- result aggregation
- best-price selection

## Run

Requires JDK 17+ and Maven.

```bash
mvn clean compile
java -cp target/classes dev.xbhou.javalab.quote.App
```

## Things to observe

Provider C intentionally exceeds the timeout and should not prevent the other providers from producing a result.

The important design idea is that one provider failure should degrade the candidate set, not fail the whole aggregation request.

## Next Experiments

- overall request deadline
- cancellation
- retry with backoff
- provider health score
- dynamic routing
- Redis quote cache
