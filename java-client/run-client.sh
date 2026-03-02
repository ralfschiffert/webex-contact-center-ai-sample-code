#!/bin/bash

# Webex Contact Center AI Streaming Insight Client Runner
echo "=== Webex Contact Center AI Streaming Insight Client Runner ==="

# Check if JAR file exists
JAR_FILE="build/libs/java-client-1.0.0.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo "❌ JAR file not found: $JAR_FILE"
    echo "Please run 'gradle clean build --no-daemon' first"
    exit 1
fi

echo "✅ Found JAR file: $JAR_FILE"

# Check command line arguments
ACCESS_TOKEN="$1"
SERVER_HOST="${2:-serving-api-streaming.wxcc-us1.cisco.com}"
SERVER_PORT="${3:-443}"

echo ""
echo "Configuration:"
echo "  Server: $SERVER_HOST:$SERVER_PORT"

if [ -z "$ACCESS_TOKEN" ]; then
    echo "  Token: ❌ Not provided"
    echo ""
    echo "Usage: $0 <access_token> [server_host] [server_port]"
    echo ""
    echo "Example:"
    echo "  $0 your-access-token-here"
    echo "  $0 your-token api-server.com 443"
    echo ""
    echo "💡 You can also run without arguments and provide the token interactively"
    echo ""
    read -p "Do you want to continue without a token? (y/N): " continue_choice
    if [[ ! "$continue_choice" =~ ^[Yy]$ ]]; then
        exit 1
    fi
    echo "  Token: ⚠️  Will be prompted interactively"
else
    echo "  Token: ✅ Provided"
fi

echo ""
echo "🚀 Starting Webex Contact Center AI Streaming Insight Client..."
echo "   Press Ctrl+C to exit"
echo ""

# Run the application
java -jar "$JAR_FILE" "$ACCESS_TOKEN" "$SERVER_HOST" "$SERVER_PORT"
