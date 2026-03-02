#!/bin/bash

# Simple Java 17 validation test script

echo "=== Java 17 Migration Validation ==="

# Check Java version
echo "1. Checking Java version..."
if java -version 2>&1 | grep -q "version \"1[7-9]\|version \"[2-9][0-9]"; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
    echo "✅ Java version $JAVA_VERSION detected (17+ required)"
else
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 2>/dev/null || echo "unknown")
    echo "❌ Java 17+ required, but found: $JAVA_VERSION"
    echo "Please install Java 17+ using the JAVA_SETUP.md guide"
    exit 1
fi

# Check JAVA_HOME
echo "2. Checking JAVA_HOME..."
if [ -n "$JAVA_HOME" ]; then
    echo "✅ JAVA_HOME is set: $JAVA_HOME"
else
    echo "⚠️  JAVA_HOME is not set (recommended but not required for gradle wrapper)"
fi

# Check build.gradle settings
echo "3. Checking build.gradle Java version settings..."
if grep -q "JavaVersion.VERSION_17" build.gradle; then
    echo "✅ build.gradle is configured for Java 17"
else
    echo "❌ build.gradle is not properly configured for Java 17"
    exit 1
fi

# Check Gradle wrapper version
echo "4. Checking Gradle wrapper version..."
if grep -q "gradle-8.5" gradle/wrapper/gradle-wrapper.properties; then
    echo "✅ Gradle wrapper 8.5 configured"
else
    echo "⚠️  Gradle wrapper may need updating"
fi

echo ""
echo "=== Validation Complete ==="
echo "✅ Java 17 migration validation passed!"
echo ""
echo "Next steps:"
echo "  1. Try building: gradle clean build --no-daemon"
echo "  2. Or use system gradle if wrapper has issues"
echo "  3. See JAVA_SETUP.md for installation help"
echo ""
