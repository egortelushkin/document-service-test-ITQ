# ========== BUILD ==========
FROM gradle:8.3-jdk17 AS builder
WORKDIR /home/gradle/project
COPY . .

# Собираем корневой проект
RUN gradle clean bootJar --no-daemon

# ========== RUN ==========
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

COPY --from=builder /home/gradle/project/build/libs/*.jar app.jar

ENTRYPOINT ["java","-jar","app.jar"]
