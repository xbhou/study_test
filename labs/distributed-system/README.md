# Distributed Systems

Small system-design experiments for idempotency, retries, rate limiting, circuit breaking, routing, and aggregation.

## Labs

### Retry + Exponential Backoff + Jitter + Deadline

Path: `retry-backoff-deadline/`

Focus:

- retryable vs non-retryable failures
- exponential backoff
- full jitter
- deadline budgeting
- retry amplification
- why retry ownership must be explicit

The next natural topic is idempotency, because retries are only safe when repeated execution cannot create duplicate side effects.
