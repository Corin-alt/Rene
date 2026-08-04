# Build stage
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app

# Copy Gradle wrapper and build files
COPY gradlew gradlew.bat ./
COPY gradle ./gradle
COPY build.gradle.kts settings.gradle.kts* ./

# Make gradlew executable
RUN chmod +x gradlew

# Download dependencies first (cached layer)
RUN ./gradlew dependencies --no-daemon || true

# Copy source code and resources
COPY src ./src

# Build the shadow JAR
RUN ./gradlew shadowJar --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre

WORKDIR /app

RUN apt-get update && apt-get install -y \
    dos2unix \
    && rm -rf /var/lib/apt/lists/*

RUN mkdir -p /app/rene-data /app/defaults

# Copy the shadow JAR from builder
COPY --from=builder /app/build/libs/*.jar /app/rene.jar

# Copy configuration files
COPY .env.example /app/.env.example
COPY .env /app/.env

# Copy default resources
COPY src/main/resources/holidays.json /app/defaults/holidays.json

# Copy and prepare startup script
COPY start.sh /app/start.sh
RUN dos2unix /app/start.sh && chmod +x /app/start.sh

# Data volume
VOLUME ["/app/rene-data"]

CMD ["/app/start.sh"]
