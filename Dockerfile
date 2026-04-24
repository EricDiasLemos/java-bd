# Stage 1 — compilação
FROM eclipse-temurin:17 AS builder
RUN apt-get update && apt-get install -y maven
WORKDIR /app
COPY . .
RUN mvn package -DskipTests

# Stage 2 — execução
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target /app/target
COPY --from=builder /app/public /app/public
COPY --from=builder /app/pom.xml /app/pom.xml
CMD ["java", "-cp", "target/classes:target/dependency/*", "Servidor"]
