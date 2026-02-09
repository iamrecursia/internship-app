FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .

RUN sed -i '/<modules>/,/<\/modules>/d' pom.xml

COPY user-service/pom.xml user-service/
COPY user-service/src user-service/src/

WORKDIR /app/user-service
RUN mvn dependency:go-offline -B

RUN mvn clean package -DskipTests -B --no-transfer-progress


FROM eclipse-temurin:21-jre-jammy

RUN addgroup --system --gid 1001 appuser && \
    adduser --system --uid 1001 --gid 1001 --shell /bin/false appuser

WORKDIR /app

COPY --from=build /app/user-service/target/*.jar ./app.jar

RUN chown appuser:appuser ./app.jar && \
    chmod 644 ./app.jar

USER appuser

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "./app.jar"]