# Use the official OpenJDK image as a parent image
FROM registry.cs.ui.ac.id/pkpl/base/eclipse-temurin:21-jdk-alpine

# Set working directory
WORKDIR /app

# Copy Gradle files for dependency resolution
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Make gradlew executable
RUN chmod +x ./gradlew

# Download dependencies (this layer will be cached unless the dependency files change)
RUN ./gradlew dependencies

# Copy the source code
COPY src src

# Build the application
RUN ./gradlew build -x test

# Create a lightweight JRE image
FROM registry.cs.ui.ac.id/pkpl/base/eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=0 /app/build/libs/auth-service-0.0.1-SNAPSHOT.jar app.jar

# Expose the port the app runs on
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-Xms128m", "-Xmx512m", "-jar", "app.jar"]
