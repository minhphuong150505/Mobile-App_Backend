# Multi-stage build for production
# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Copy Maven wrapper
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Copy source code
COPY src src/

# Make mvnw executable
RUN chmod +x mvnw

# Build the application
RUN ./mvnw clean package -DskipTests -q

# Stage 2: Run
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Install curl for health checks
RUN apk add --no-cache curl

# Copy JAR from build stage
COPY --from=build /app/target/camera-shop-backend-1.0.0.jar app.jar

# Copy health check script
COPY health-check.sh /health-check.sh
RUN chmod +x /health-check.sh

# Expose the application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD /health-check.sh || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
