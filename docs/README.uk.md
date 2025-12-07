# 'Seezu' API-service (course work 2025)

Backend для мобільного застосунку **'Seezu'**.
[🇺🇸 English](../README.md) | [🇺🇦 Українська](README.uk.md)

## Технології

- Java 21
- Spring Boot
- Spring Data JPA / Hibernate
- PostgreSQL
- Maven
- Docker
- GraalVM
- MapStruct
- Flyway
- OpenAPI / Swagger

## Функціонал

- Аутентифікація та авторизація користувачів
- CRUD операції для курсів
- ендпоінти для доєднання до курсів, прохід по темах та заповнення конспектів
- Кешування за допомогою Spring Cache
- Валідація та обробка помилок
- REST API для клієнта

## API

Всі ендпоінти знаходяться за базовим шляхом /api.

#### Приклади:

- **GET /api/course/** — отримати список власних курсів
- **PUT /api/cards/{flashCardId}/concept** — оновити концепт флешкартки

Повна документація Swagger доступна за шляхом `/api/swagger-ui/index.html` після запуску програми.

## Початок роботи
### Крок 1. Клонування репозиторію
```bash
git clone https://github.com/AndriyKramar2288/cinema_server.git
cd repo
```
### Крок 2. Налаштування БД
Для роботи застосунку необхідна БД postgresql. Попередньо підготуйте її (жодних схем застосовувати не потрібно).
### Крок 3. Створення `application-dev.yaml`
Створіть файл `application-dev.yaml` за шляхом `./cw2025_backend_core/src/main/resources/application-dev.yaml` з наступним вмістом:
```yaml
spring:
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
При цьому вкажіть усі необхідні дані, що позначені **коментарями**.
### Крок 4. Запуск
#### Запуск за замовчуванням
```bash
cd ./cw2025_backend_core
../mvnw spring-boot:run
```
Зверніть увагу, що для цього необхідно попередньо виконати установку залежностей в **локальний репозиторій**:
```bash
./mvnw clean install -DskipTests
```
#### Збірка та запуск через JAR
```bash
./mvnw clean package -DskipTests
java -jar target/app.jar
```
### Крок 5. Деплой
#### Увага!

Наразі в проекті наявний **github-action** `.github\workflows\docker-image.yml`, який автоматично побудує
та запушить образ при будь-якому push на `master` гілку.

---
Спершу слід побудувати та запушити образ на Docker Hub.
**Є два варіанти**: будувати образ, що звично збирає застосунок в JAR та запускає з-під JVM, або ж будувати виконуваний
файл через GraalVM, що значно підвищує швидкість запуску програми, проте може спровокувати багато проблем.
#### Збірка образу з JAR
```bash
docker build -t cw2025_backend_image .
```
#### Збірка образу з файлом GraalVM
```bash
docker build -f Dockerfile.native -t cw2025_backend_image .
```
Після побудови образу, його слід запушити на Docker Hub.
Поточне розташування чинного контейнера (образу): `banew/cw2025_backend_docker_repo:cw_img`
```bash
docker tag cw2025_backend_image:latest <container tag>
docker push <container tag>
# for the current location
docker tag cw2025_backend_image:latest banew/cw2025_backend_docker_repo:cw_img
docker push banew/cw2025_backend_docker_repo:cw_img
```
Зазначимо, що в середовищі, з-під якого запускатиметься контейнтер, слід вказати наступні змінні:
```env
LOCAL_DB_URL= # production db-url, that looks like 'jdbc:postgresql://<database host>:<database port>/<database name>'
LOCAL_DB_PASSWORD= # production postgres password
LOCAL_DB_USER= # production postgres user
SECRET_DECODE_KEY= # production secret decode key
SPRING_PROFILES_ACTIVE=prod
```
