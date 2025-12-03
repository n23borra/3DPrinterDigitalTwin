FROM maven:3.9.5-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom first to leverage Docker layer caching for dependencies
COPY pom.xml .

# Copy the remainder of the source and build the application
COPY src ./src
RUN mvn clean package -DskipTests
#RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]