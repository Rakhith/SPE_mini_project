# ---------- Stage 1: build ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom first so dependencies are cached as their own layer
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------- Stage 2: runtime ----------
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Run as a non-root user
RUN groupadd -r appgroup && useradd -r -g appgroup appuser

COPY --from=build /app/target/calculator.jar app.jar

USER appuser
EXPOSE 8080
ENV PORT=8080

ENTRYPOINT ["java", "-jar", "app.jar"]
