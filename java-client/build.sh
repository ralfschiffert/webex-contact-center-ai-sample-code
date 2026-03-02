#!/bin/bash

# Build script for Webex Contact Center AI Streaming Insight Java Client

set -e  # Exit on any error

echo "=== Building Webex Contact Center AI Streaming Insight Java Client ==="

# Check if Java is available
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed or not in PATH"
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1-2)
echo "Using Java version: $JAVA_VERSION"

# Check if Java version is 17 or higher
if ! java -version 2>&1 | grep -q "version \"1[7-9]\|version \"[2-9][0-9]"; then
    echo "Error: Java 17 or higher is required"
    echo "Current version: $JAVA_VERSION"
    echo "Please install Java 17+ and set JAVA_HOME appropriately"
    exit 1
fi

# Make gradlew executable
chmod +x ./gradlew

echo "Cleaning previous build..."
./gradlew clean

echo "Copying protobuf files..."
./gradlew copyProtos

echo "Generating protobuf classes..."
./gradlew generateProto

echo "Compiling Java classes..."
./gradlew compileJava

echo "Running tests..."
./gradlew test

echo "Building JAR..."
./gradlew jar

echo ""
echo "=== Build Complete! ==="
echo "JAR file: build/libs/java-client-1.0.0.jar"
echo ""
echo "To run the client:"
echo "  ./run.sh"
echo "  or"
echo "  java -jar build/libs/java-client-1.0.0.jar [host] [port] [token]"
echo ""
echo "To run with Gradle:"
echo "  ./gradlew run"
echo ""
