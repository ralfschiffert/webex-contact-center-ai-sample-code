# Java 17 Setup Guide

This project requires **Java 17 or higher**. Here's how to install and configure it on different platforms:

## 🔍 Check Current Java Version

```bash
java -version
```

If you see `1.8`, `11.x`, or similar, you need to upgrade to Java 17+.

## 📥 Installation Options

### macOS

#### Option 1: Using Homebrew (Recommended)
```bash
# Install OpenJDK 17
brew install openjdk@17

# Create symlink
sudo ln -sfn /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk

# Set JAVA_HOME (add to ~/.zshrc or ~/.bash_profile)
export JAVA_HOME=/Library/Java/JavaVirtualMachines/openjdk-17.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH
```

#### Option 2: Using SDKMAN
```bash
# Install SDKMAN
curl -s "https://get.sdkman.io" | bash
source ~/.sdkman/bin/sdkman-init.sh

# Install Java 17
sdk install java 17.0.9-tem
sdk use java 17.0.9-tem
```

#### Option 3: Manual Download
1. Download from [Adoptium](https://adoptium.net/temurin/releases/?version=17)
2. Install the `.pkg` file
3. Set JAVA_HOME in your shell profile

### Linux (Ubuntu/Debian)

```bash
# Update package index
sudo apt update

# Install OpenJDK 17
sudo apt install openjdk-17-jdk

# Set JAVA_HOME (add to ~/.bashrc or ~/.profile)
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH

# Reload environment
source ~/.bashrc
```

### Linux (CentOS/RHEL/Fedora)

```bash
# Install OpenJDK 17
sudo yum install java-17-openjdk-devel  # CentOS/RHEL 7
# or
sudo dnf install java-17-openjdk-devel  # CentOS/RHEL 8+ / Fedora

# Set JAVA_HOME
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
export PATH=$JAVA_HOME/bin:$PATH
```

### Windows

#### Option 1: Using Chocolatey
```powershell
# Install Chocolatey (if not already installed)
Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://chocolatey.org/install.ps1'))

# Install OpenJDK 17
choco install openjdk17
```

#### Option 2: Manual Download
1. Download from [Adoptium](https://adoptium.net/temurin/releases/?version=17)
2. Install the `.msi` file
3. Set JAVA_HOME environment variable in System Properties

## 🔧 Managing Multiple Java Versions

### Using SDKMAN (Linux/macOS)
```bash
# List available Java versions
sdk list java

# Install multiple versions
sdk install java 11.0.21-tem
sdk install java 17.0.9-tem

# Switch between versions
sdk use java 17.0.9-tem      # For current session
sdk default java 17.0.9-tem  # Set as default
```

### Using update-alternatives (Linux)
```bash
# Install multiple JDK versions
sudo apt install openjdk-11-jdk openjdk-17-jdk

# Configure alternatives
sudo update-alternatives --config java
sudo update-alternatives --config javac

# Set JAVA_HOME dynamically
export JAVA_HOME=$(update-alternatives --query javac | sed -n -e 's/Best: *\(.*\)\/bin\/javac/\1/p')
```

## ✅ Verification

After installation, verify your setup:

```bash
# Check Java version (should show 17 or higher)
java -version

# Check Java compiler version
javac -version

# Verify JAVA_HOME is set
echo $JAVA_HOME

# Test the project build
./build.sh
```

Expected output for Java version:
```
openjdk version "17.0.9" 2023-10-17
OpenJDK Runtime Environment Temurin-17.0.9+9 (build 17.0.9+9)
OpenJDK 64-Bit Server VM Temurin-17.0.9+9 (build 17.0.9+9, mixed mode)
```

## 🚨 Troubleshooting

### "JAVA_HOME not set" Error
```bash
# Find Java installation path
which java
/usr/bin/java -XshowSettings:properties -version 2>&1 | grep "java.home"

# Set JAVA_HOME (example)
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home
```

### Multiple Java Versions Conflict
```bash
# Check which Java is being used
which java
ls -la /usr/bin/java

# Update PATH to prioritize correct Java
export PATH=/path/to/java-17/bin:$PATH
```

### Permission Issues (Linux/macOS)
```bash
# If you get permission errors
sudo chown -R $(whoami) /path/to/java-17
chmod +x /path/to/java-17/bin/*
```

## 📚 IDE Configuration

### IntelliJ IDEA
1. File → Project Structure → Project Settings → Project
2. Set Project SDK to Java 17
3. Set Project Language Level to 17

### VS Code
1. Install "Language Support for Java" extension
2. Open Command Palette (Ctrl/Cmd + Shift + P)
3. Run "Java: Configure Runtime"
4. Set Java 17 as the runtime

### Eclipse
1. Window → Preferences → Java → Installed JREs
2. Add Java 17 JRE
3. Set as default

## 🌐 Recommended Java Distributions

1. **[Eclipse Temurin](https://adoptium.net/)** - OpenJDK builds by Eclipse Foundation (Recommended)
2. **[Amazon Corretto](https://aws.amazon.com/corretto/)** - Free, multiplatform OpenJDK distribution
3. **[Oracle OpenJDK](https://openjdk.java.net/)** - Reference implementation
4. **[Red Hat OpenJDK](https://developers.redhat.com/products/openjdk/download)** - Enterprise-focused builds

All of these are compatible with this project. Choose based on your preference and organizational requirements.

---

Once you have Java 17+ installed and configured, you can proceed with building the Streaming Insight Client:

```bash
./build.sh
```
