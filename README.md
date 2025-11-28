# 'Seezu' API-service (course work 2025)

Backend for the **'Seezu'** mobile application.
[🇺🇸 English](README.md) | [🇺🇦 Українська](docs/README.uk.md)

## Technologies

- Java 21
- Spring Boot
- Spring Data JPA / Hibernate
- PostgreSQL
- Maven
- Docker
- GraalVM

## Features

- User authentication and authorization
- CRUD operations for courses
- Endpoints for joining courses, progressing through topics, and filling out compendia
- Caching using Spring Cache
- Validation and error handling
- REST API for client

## API

All endpoints are located under the base path /api.

#### Examples:

- **GET /api/course/** — get list of own courses
- **PUT /api/cards/{flashCardId}/concept** — update flashcard concept

Full Swagger documentation is available at `/api/swagger-ui/index.html` after launching the application.

## Getting Started
### Step 1. Clone the repository
```bash
git clone https://github.com/AndriyKramar2288/cinema_server.git
cd repo
```
### Step 2. Database setup
The application requires a PostgreSQL database. Prepare it in advance (no schemas need to be applied).
### Step 3. Create `application-dev.yaml`
Create an `application-dev.yaml` file at `./cw2025_backend_core/src/main/resources/application-dev.yaml` with the following content:
```yaml
spring:
  config:
    activate:
      on-profile: dev
  datasource:
    username: # your postgres user
    password: # your postgres password
    url: # db-url, that looks like 'jdbc:postgresql://<your database host>:<database port>/<database name>'
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        generate_statistics: true

logging:
  level:
    org.springframework.web.servlet: DEBUG

secret.decoder_key: # any long string secret key
```
Make sure to specify all necessary data marked by **comments**.
### Step 4. Running
#### Default run
```bash
cd ./cw2025_backend_core
../mvnw spring-boot:run
```
Note that this requires dependencies to be installed in the **local repository** beforehand:
```bash
./mvnw clean install -DskipTests
```
#### Build and run via JAR
```bash
./mvnw clean package -DskipTests
java -jar target/app.jar
```
### Step 5. Deployment
#### Attention!

Currently, the project has a **github-action** `.github\workflows\docker-image.yml`, which will automatically build
and push the image when any push to the `master` branch is made.
---
First, you need to build and push the image to Docker Hub.
**There are two options**: build an image that conventionally packages the application as a JAR and runs under JVM, or build an executable file via GraalVM, which significantly improves startup time but may cause many issues.
#### Build image with JAR
```bash
docker build -t cw2025_backend_image .
```
#### Build image with GraalVM executable
```bash
docker build -f Dockerfile.native -t cw2025_backend_image .
```
After building the image, it should be pushed to Docker Hub.
Current location of the active container (image): `banew/cw2025_backend_docker_repo:cw_img`
```bash
docker tag cw2025_backend_image:latest <container tag>
docker push <container tag>
# for the current location
docker tag cw2025_backend_image:latest banew/cw2025_backend_docker_repo:cw_img
docker push banew/cw2025_backend_docker_repo:cw_img
```
Note that the environment where the container will run should specify the following variables:
```env
LOCAL_DB_URL= # production db-url, that looks like 'jdbc:postgresql://<database host>:<database port>/<database name>'
LOCAL_DB_PASSWORD= # production postgres password
LOCAL_DB_USER= # production postgres user
SECRET_DECODE_KEY= # production secret decode key
SPRING_PROFILES_ACTIVE=prod
```
