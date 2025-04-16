#!/bin/bash
# Check if .env exists, if not, create it from .env.example
if [ ! -f .env ] && [ -f .env.example ]; then
    echo "Creating .env from .env.example"
    cp .env.example .env
    echo "Please edit the .env file to set your Discord bot token"
    echo "Then run this script again"
    exit 1
elif [ ! -f .env ]; then
    echo "No .env or .env.example file found!"
    echo "Please create a .env file with your Discord bot token (BOT_TOKEN=your_token_here)"
    exit 1
fi

# Check that the token is properly defined
if grep -q "BOT_TOKEN=your_discord_bot_token_here\|BOT_TOKEN=$" .env; then
    echo "Error: BOT_TOKEN is not properly set in the .env file!"
    echo "Please edit the .env file to add your Discord token."
    exit 1
fi

echo "Building Docker image..."
docker-compose build

echo "Starting container..."
docker-compose up -d

echo "Displaying logs (Ctrl+C to quit)..."
docker-compose logs -f