FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
COPY api-gateway/pom.xml api-gateway/

RUN mvn dependency:go-offline -B -pl api-gateway -am

COPY api-gateway/src ./api-gateway/src
RUN mvn package -DskipTests -B -pl api-gateway -am

FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/api-gateway/target/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]