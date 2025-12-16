# Step 1: Build with Maven
FROM maven:3.9.5-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

# BuildKit cache for Maven dependencies
RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests

# Step 2: Lightweight runtime image (JRE)
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
