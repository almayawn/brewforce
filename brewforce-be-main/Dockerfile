# Use the official OpenJDK image as a parent image
FROM eclipse-temurin:17-jdk-alpine

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
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=0 /app/build/libs/backend-0.0.1-SNAPSHOT.jar app.jar

# Expose the port the app runs on
EXPOSE 8081
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"
# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]