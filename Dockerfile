# Use a Java base image
FROM eclipse-temurin:17-jdk

# Set the working directory
WORKDIR /app

# Install necessary tools
RUN apt-get update && apt-get install -y \
    wget \
    dos2unix \
    && rm -rf /var/lib/apt/lists/*

# Create necessary directories
RUN mkdir -p /app/out /app/libs /app/rene-data

# Download JDA and its dependencies
RUN mkdir -p /app/libs && \
    cd /app/libs && \
    wget https://github.com/discord-jda/JDA/releases/download/v5.3.2/JDA-5.3.2-withDependencies.jar && \
    wget https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/2.0.7/slf4j-simple-2.0.7.jar && \
    wget https://repo1.maven.org/maven2/org/reflections/reflections/0.10.2/reflections-0.10.2.jar && \
    wget https://repo1.maven.org/maven2/org/javassist/javassist/3.29.2-GA/javassist-3.29.2-GA.jar && \
    wget https://repo1.maven.org/maven2/com/google/code/findbugs/jsr305/3.0.2/jsr305-3.0.2.jar && \
    wget https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.42.0.0/sqlite-jdbc-3.42.0.0.jar

# Copy the source code
COPY src /app/src

# Copy the configuration files
COPY .env.example /app/.env.example

# Copy the startup script
COPY start.sh /app/start.sh
# Convert from DOS to UNIX format to avoid CRLF issues
RUN dos2unix /app/start.sh && chmod +x /app/start.sh

# Compile the source code
RUN javac -cp "/app/libs/*" -d /app/out $(find /app/src -name "*.java")

# Create the data folder and empty properties file (fallback)
RUN mkdir -p /app/rene-data && \
    echo "# Bot properties" > /app/rene-data/bot.properties

# Expose the data folder as a volume
VOLUME ["/app/rene-data"]

# Run the startup script
CMD ["/app/start.sh"]