# 'Seezu' API-service (course work 2025)

Backend для мобільного застосунку 'Seezu'.

## Технології

- Java 21
- Spring Boot
- Spring Data JPA / Hibernate
- PostgreSQL
- Maven
- Docker

## Функціонал

- Аутентифікація та авторизація користувачів
- CRUD операції для курсів
- ендпоінти для доєднання до курсів, прохід по темах та заповнення конспектів
- Кешування за допомогою Spring Cache
- Валідація та обробка помилок
- REST API для фронтенду

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
вказавши усі необхідні дані, що позначені коментарями.
### Крок 4. Запуск
#### Запуск за замовчуванням
```bash
./mvnw spring-boot:run
```
#### Збірка та запуск через JAR
```bash
./mvnw clean package
java -jar target/app.jar
```
### Крок 5. Деплой
Спершу слід побудувати та запушити контейнер на Docker Hub.
Поточне розташування діючого контейнера: `banew/cw2025_backend_docker_repo:cw_img`
```bash
docker build . -t cw2025_backend_image
docker tag cw2025_backend_image:latest <container tag>
docker push <container tag>
```
Зазначимо, що в середовищі, з-під якого запускатиметься контейнтер, слід вказати наступні змінні:
```env
LOCAL_DB_URL= # production db-url, that looks like 'jdbc:postgresql://<database host>:<database port>/<database name>'
LOCAL_DB_PASSWORD= # production postgres password
LOCAL_DB_USER= # production postgres user
SECRET_DECODE_KEY= # production secret decode key
SPRING_PROFILES_ACTIVE=prod
```
