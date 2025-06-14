# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Declare a volume for persistent database storage
VOLUME /app/data

# Copy the executable JAR file from your build directory to the container
# Adjust the path if your JAR file is named differently or located elsewhere
COPY target/*.jar app.jar

# Make port 8081 available to the world outside this container
# This should match the server.port in your application.properties
EXPOSE 8081

# Run the JAR file
ENTRYPOINT ["java","-jar","/app/app.jar"]