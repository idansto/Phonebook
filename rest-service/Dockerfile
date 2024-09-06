# Use a base image with Java installed
FROM openjdk:17-jdk-alpine

# Set the working directory in the container
WORKDIR /app

# Copy the jar file into the container
COPY target/rest-service-0.0.1-SNAPSHOT.jar /app/rest-service-0.0.1-SNAPSHOT.jar

# Specify the command to run the jar
ENTRYPOINT ["java", "-jar", "/app/rest-service-0.0.1-SNAPSHOT.jar"]

EXPOSE 8080