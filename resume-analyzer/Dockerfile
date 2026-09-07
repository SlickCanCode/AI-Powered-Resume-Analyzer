# ---- Build & Test stage ----
FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

# Copy build files first for better layer caching
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
COPY src ./src


# Run tests as part of the build (fails the build if tests fail)
RUN ./mvnw test

# Package the application (skip re-running tests since we just ran them)
RUN ./mvnw package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre AS runtime

WORKDIR /app

# Copy only the built jar from the build stage — nothing else carries over
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]