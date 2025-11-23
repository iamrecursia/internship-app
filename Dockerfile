# Этап сборки
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# Копируем корневой pom.xml
COPY pom.xml .

# Копируем папку order-service
COPY order-service ./order-service

# Собираем ТОЛЬКО order-service с зависимостями
RUN mvn -pl order-service clean package -DskipTests -B --no-transfer-progress

# Runtime stage
FROM eclipse-temurin:21-jre-jammy

# Создаём непривилегированного пользователя
RUN addgroup --system --gid 1001 appgroup && \
    adduser --system --uid 1001 --gid 1001 --shell /bin/false appuser

WORKDIR /app

COPY --from=build /app/order-service/target/*.jar app.jar

RUN chown appuser:appgroup app.jar
USER appuser

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]