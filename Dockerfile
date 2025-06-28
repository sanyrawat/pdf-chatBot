# Use Java 17 image
FROM eclipse-temurin:17-jdk

# Set work directory
WORKDIR /app

# Copy your built JAR (adjust name if needed)
COPY target/*.jar genAi-chatBot-0.0.1-SNAPSHOT.jar

# Expose port (must be 8080 for Render)
EXPOSE 8080

# Run the jar
ENTRYPOINT ["java", "-jar", "genAi-chatBot-0.0.1-SNAPSHOT.jar"]
