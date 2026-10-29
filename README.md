# Calculator DevOps

A simple Calculator REST API in plain Java (JDK built-in HTTP server, Maven, JUnit 5),
with a full CI/CD pipeline: GitHub -> Jenkins -> Docker -> Docker Hub -> Docker Compose.

Docker Hub image: [rakhith/calculator](https://hub.docker.com/r/rakhith/calculator)

## Endpoints

| Operation | Endpoint |
| --- | --- |
| Add | GET /api/v1/add?a=&b= |
| Subtract | GET /api/v1/subtract?a=&b= |
| Multiply | GET /api/v1/multiply?a=&b= |
| Divide | GET /api/v1/divide?a=&b= (b=0 returns HTTP 400) |
| Health | GET /health |

Example response: `{"operation":"add","a":2,"b":3,"result":5}`
Errors return HTTP 400: `{"error":"Division by zero is not allowed","status":400}`

## Build, test, run

    mvn clean package          # build the JAR (runs tests)
    mvn test                   # run tests only
    java -jar target/calculator.jar
    curl "http://localhost:8080/api/v1/add?a=2&b=3"

The port can be changed with the `PORT` environment variable.

## Docker

    docker build -t calculator .
    docker run -p 8081:8080 calculator

## Docker Compose

    docker compose up -d

The Nginx proxy is exposed on http://localhost:8082 (2 calculator replicas behind Nginx, with health checks).

## CI/CD

The `Jenkinsfile` defines the pipeline: Checkout, Build, Test, Docker Build, Push, Deploy.

Pipeline test: automatic trigger.

## Project report and evidence

- [Project report (PDF)](PROJECT_REPORT.pdf)
- [Screenshots](screenshots/)
