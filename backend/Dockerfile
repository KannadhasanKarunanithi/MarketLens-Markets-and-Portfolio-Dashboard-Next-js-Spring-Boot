# Build
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw -B --no-transfer-progress dependency:go-offline
COPY src src
RUN ./mvnw -B --no-transfer-progress clean package -DskipTests

# Run
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --home /app appuser
COPY --from=build /app/target/marketlens.jar app.jar
USER appuser
ENV JAVA_OPTS="-Duser.timezone=UTC -XX:MaxRAMPercentage=75"
EXPOSE 8090
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
