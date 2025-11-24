# syntax=docker/dockerfile:1

FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /build

COPY --chmod=0755 mvnw mvnw
COPY .mvn/ .mvn/

# Copy entire project
COPY . .

# Build ALL modules, including common + core
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw clean package -DskipTests

# Copy final jar from core
RUN cp cw2025_backend_core/target/*.jar /build/app.jar


FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=build /build/app.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
