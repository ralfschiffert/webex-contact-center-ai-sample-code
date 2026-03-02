#!/bin/bash

# Run script for Webex Contact Center AI Streaming Insight Java Client

JAR_FILE="build/libs/java-client-1.0.0.jar"

# Check if JAR exists
if [ ! -f "$JAR_FILE" ]; then
    echo "JAR file not found: $JAR_FILE"
    echo "Please build the project first:"
    echo "  ./build.sh"
    echo "  or"
    echo "  ./gradlew build"
    exit 1
fi

# Create logs directory if it doesn't exist
mkdir -p logs

echo "=== Starting Webex Contact Center AI Streaming Insight Client ==="
echo "JAR: $JAR_FILE"

# Pass all command line arguments to the Java application
if [ $# -eq 0 ]; then
    echo "Running in interactive mode..."
    echo "Arguments: [none - using defaults]"
else
    echo "Arguments: $@"
fi

# Set JVM options
JVM_OPTS="-Xmx1g -Xms256m"

# Add debug options if DEBUG environment variable is set
if [ ! -z "$DEBUG" ]; then
    JVM_OPTS="$JVM_OPTS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
    echo "Debug mode enabled on port 5005"
fi

# Run the application
java $JVM_OPTS -jar "$JAR_FILE" "$@"
