FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests -B --no-transfer-progress

FROM eclipse-temurin:21-jre-jammy

RUN addgroup --system --gid 1001 appgroup && \
    adduser --system --uid 1001 --gid 1001 --shell /bin/false appuser

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

RUN chown appuser:appgroup app.jar && chmod 644 app.jar
USER appuser

EXPOSE 8761
ENTRYPOINT ["java", "-jar", "/app/app.jar"]