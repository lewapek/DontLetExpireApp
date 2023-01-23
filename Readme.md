# DontLetExpire app

## Description

Simple app demonstrating the following stuff in action:
- Scala3
- ZIO2
- Cats
- Caliban (Graphql)
- Http4s
- Circe (Json)
- Doobie (with Postgres)

With the app you can monitor products you bought, so if any of the product is about to expire you can view in on the top of the list - therefore you are more likely to not let it expire ;)

## Endpoints

- /graphql
- /healthcheck

## Containerization

App can be containerized using Docker.  

### Building

```shell
./build.sh
```

### Running container locally

You need to build first and then:
```shell
./local-postgres.sh restart
docker run dontletexpireapp:$(head -1 version)
```

## Tests

### Unit tests

```shell
sbt test
```

### Integration tests

```shell
./local-postgres.sh restart
sbt IntegrationTest/test
```
