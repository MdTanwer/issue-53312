# Reproducer for quarkusio/quarkus#54476

Minimal project demonstrating that `quarkus.mongodb.active=false` does not skip connection string parsing.

## Issue

When `quarkus.mongodb.active` is `false`, Quarkus still parses and URL-decodes `quarkus.mongodb.connection-string` during startup. Connection strings with template placeholders (e.g. `mongodb://%s:%s@host:27017/db`) fail URL decoding and prevent application startup.

- Issue: https://github.com/quarkusio/quarkus/issues/54476
- Quarkus version: `999-SNAPSHOT` (local build from `~/quarkus`)

## Configuration

```properties
quarkus.mongodb.connection-string=mongodb://%s:%s@x.y.z.w:27017/db
quarkus.mongodb.active=false
```

## Prerequisites

Install the local Quarkus build into your Maven repository:

```bash
cd ~/quarkus
./mvnw install -DskipTests -Dquickly
```

## Sample API

Once startup succeeds (after the fix), the app exposes:

| Endpoint | Description |
|----------|-------------|
| `GET /hello` | Simple health-style greeting |
| `GET /api/mongo/template` | Shows inactive Mongo config and the template connection string |

Example:

```bash
./mvnw quarkus:dev
curl http://localhost:8080/hello
curl http://localhost:8080/api/mongo/template
```

## Run

```bash
cd mongodb-active-reproducer
./mvnw test
```

| Test | Result (current bug) |
|------|----------------------|
| `MongoActiveReproducerTest` | **Fails** — reproduces [#54476](https://github.com/quarkusio/quarkus/issues/54476) |

```bash
./mvnw test -Dtest=MongoActiveReproducerTest
```

## Expected (after fix)

Tests pass — inactive MongoDB client config is not validated or parsed.

## Actual (bug)

Startup fails with:

```
java.lang.IllegalArgumentException: URLDecoder: Incomplete trailing escape (%) pattern
    at com.mongodb.ConnectionString.<init>(...)
    at io.quarkus.mongodb.runtime.MongoClientRecorder.initializeDNSLookup(...)
    at io.quarkus.mongodb.runtime.MongoClientRecorder.performInitialization(...)
```

## Workaround

Use a syntactically valid placeholder connection string:

```properties
quarkus.mongodb.connection-string=mongodb://placeholder:placeholder@localhost:27017/db
quarkus.mongodb.active=false
```
