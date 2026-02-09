FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .

RUN sed -i '/<modules>/,/<\/modules>/d' pom.xml

RUN mvn install -N -DskipTests

COPY auth-service/src auth-service/src/
COPY auth-service/pom.xml auth-service/

RUN mvn clean package -DskipTests -B -f auth-service/pom.xml

FROM eclipse-temurin:21-jre-jammy

RUN addgroup --system --gid 1001 appgroup && \
    adduser --system --uid 1001 --gid 1001 --shell /bin/false appuser

WORKDIR /app

COPY --from=build /app/auth-service/target/*.jar app.jar

RUN chown appuser:appgroup app.jar && chmod 644 app.jar
USER appuser

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]