# Step 1: Build the application
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Step 2: Run the application
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the compiled jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Create a directory for the database to ensure it persists
RUN mkdir -p /app/data

# Set the environment variable to point to the data folder
ENV DATABASE_PATH=/app/data/database.db

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.datasource.url=jdbc:sqlite:/app/data/database.db"]