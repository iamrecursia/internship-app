FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy

RUN useradd -m -s /bin/bash appuser

WORKDIR /app

COPY --from=build /app/target/UserService-0.0.1-SNAPSHOT.jar /app/user-service.jar

RUN chown -R appuser:appuser /app

USER appuser

EXPOSE 8081

CMD ["java", "-jar", "/app/user-service.jar"]
