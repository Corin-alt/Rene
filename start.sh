#!/bin/sh

echo "Starting Rene..."

# Copy default configs into the data volume if they don't exist yet
if [ ! -f /app/rene-data/holidays.json ] && [ -f /app/defaults/holidays.json ]; then
  cp /app/defaults/holidays.json /app/rene-data/holidays.json
  echo "Copied default holidays.json into rene-data."
fi

while true; do
  echo "[$(date)] Starting Rene bot..."

  # Launch the bot with the appropriate classpath
  java -cp "/app/out:/app/libs/*" fr.corentin.rene.Rene

  # Get the exit code
  EXIT_CODE=$?

  echo "[$(date)] Bot stopped with exit code $EXIT_CODE."

  # If exit code is 0 (normal shutdown), exit the loop
  if [ $EXIT_CODE -eq 0 ]; then
    echo "Normal bot shutdown."
    break
  fi

  echo "Restarting in 10 seconds..."
  sleep 10
done