FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
RUN sed -i '/<modules>/,/<\/modules>/d' pom.xml

RUN mvn install -N -DskipTests

COPY shared-dto/pom.xml shared-dto/
COPY shared-dto/src shared-dto/src/

RUN mvn install -DskipTests -f shared-dto/pom.xml

COPY order-service ./order-service

RUN mvn package -DskipTests -f order-service/pom.xml

FROM eclipse-temurin:21-jre-jammy

RUN addgroup --system --gid 1001 appgroup && \
    adduser --system --uid 1001 --gid 1001 --shell /bin/false appuser

WORKDIR /app

COPY --from=build /app/order-service/target/order-service-*.jar /app/order-service.jar

RUN chown appuser:appgroup /app/order-service.jar && \
    chmod 644 /app/order-service.jar

USER appuser

EXPOSE 8083

CMD ["java", "-jar", "/app/order-service.jar"]