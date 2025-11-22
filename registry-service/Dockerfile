# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:21-jre

RUN addgroup --system javauser && adduser --system javauser --ingroup javauser
USER javauser

COPY --from=build target/*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "/app.jar"]