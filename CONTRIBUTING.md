# Contributing to Webex Contact Center Serving API

This repository contains enhanced implementations and documentation for the Webex Contact Center Serving API (Real-Time Transcripts API).

## Repository Structure

This is a local enhanced version of the [CiscoDevNet/webex-contact-center-ai-sample-code](https://github.com/CiscoDevNet/webex-contact-center-ai-sample-code) repository with significant improvements.

### Git Configuration

```bash
# Upstream (CiscoDevNet official repository)
upstream: https://github.com/CiscoDevNet/webex-contact-center-ai-sample-code.git

# Your fork (when you create one)
origin: https://github.com/YOUR_USERNAME/webex-contact-center-ai-sample-code.git
```

## Major Enhancements in This Version

### 1. Complete Java Client Implementation
- **Interactive CLI** with menu-driven interface
- Production-ready error handling and logging
- Multiple response handlers (Console, JSON, Transcript-specific)
- Configurable client with TLS support
- Comprehensive documentation

### 2. Enhanced Documentation
- **Getting Started Guide** (1,400+ lines) covering:
  - Real-Time Transcripts API overview
  - Relationship to Webex Agent Desktop
  - Use cases with real-world examples
  - Architecture diagrams
  - Step-by-step integration guide
  - Troubleshooting and best practices
- Java setup and migration guides
- Error fix documentation

### 3. Build System & Tooling
- Gradle build system with wrapper
- Shell scripts for building and running
- Java 17 support
- Automated protobuf code generation

## Contributing Back to CiscoDevNet

When you're ready to contribute your improvements back to the official repository:

### Step 1: Fork the Official Repository

1. Go to https://github.com/CiscoDevNet/webex-contact-center-ai-sample-code
2. Click "Fork" to create your own fork
3. Add your fork as the `origin` remote:

```bash
git remote add origin https://github.com/YOUR_USERNAME/webex-contact-center-ai-sample-code.git
```

### Step 2: Sync with Upstream

Before contributing, sync with the latest upstream changes:

```bash
# Fetch upstream changes
git fetch upstream

# Check what's different
git log upstream/main..main

# If needed, rebase your changes on top of upstream
git rebase upstream/main
```

### Step 3: Prepare Your Contribution

Since this repository contains the `serving-api` directory content, you'll need to:

1. **Create a branch for your contribution:**
```bash
git checkout -b feature/enhanced-java-client
```

2. **Structure your changes to fit the upstream repository:**
   - Your files should go into `serving-api/` directory
   - Keep the same structure: `serving-api/java-client/`, `serving-api/documentation/`, etc.

3. **Ensure your contribution includes:**
   - Updated README.md explaining the Java client
   - All necessary build files and dependencies
   - Documentation for new features
   - Examples and usage instructions

### Step 4: Create a Pull Request

1. **Push to your fork:**
```bash
git push origin feature/enhanced-java-client
```

2. **Create PR on GitHub:**
   - Go to your fork on GitHub
   - Click "New Pull Request"
   - Base repository: `CiscoDevNet/webex-contact-center-ai-sample-code`
   - Base branch: `main`
   - Head repository: `YOUR_USERNAME/webex-contact-center-ai-sample-code`
   - Compare branch: `feature/enhanced-java-client`

3. **PR Description should include:**
   - Summary of enhancements
   - Why these improvements are valuable
   - How to test the new features
   - Any breaking changes or migration notes

### Example PR Description

```markdown
# Enhanced Serving API with Production-Ready Java Client

## Summary
This PR adds a comprehensive Java client implementation for the Serving API with an interactive CLI, production-ready error handling, and extensive documentation.

## Key Features
- **Interactive CLI**: Menu-driven interface for testing all API operations
- **Multiple Response Handlers**: Console, JSON, and transcript-specific handlers
- **Production-Ready**: Comprehensive error handling, logging, and retry logic
- **Complete Documentation**: Getting Started guide, Java setup guides, troubleshooting

## What's New
1. Complete Java 17 client implementation (`java-client/`)
2. Interactive CLI with 5 operation modes
3. Gradle build system with wrapper
4. Comprehensive Getting Started guide (1,400+ lines)
5. Shell scripts for easy building and running

## Testing
```bash
cd serving-api/java-client
./gradlew build
./gradlew run
```

## Breaking Changes
None - this is purely additive to the existing JavaScript client.

## Documentation
- See `serving-api/documentation/GETTING_STARTED.md` for complete guide
- See `serving-api/java-client/README.md` for Java client usage
```

## Development Workflow

### Making Changes

```bash
# Create a feature branch
git checkout -b feature/your-feature-name

# Make your changes
# ... edit files ...

# Commit with descriptive messages
git add .
git commit -m "Add interactive CLI menu system"

# Push to your fork (when you have one)
git push origin feature/your-feature-name
```

### Keeping Up to Date

```bash
# Fetch latest from upstream
git fetch upstream

# Merge upstream changes into your main branch
git checkout main
git merge upstream/main

# Rebase your feature branch
git checkout feature/your-feature-name
git rebase main
```

## Code Style Guidelines

### Java
- Follow standard Java conventions
- Use meaningful variable and method names
- Add JavaDoc comments for public APIs
- Include error handling and logging
- Write clean, maintainable code

### Documentation
- Use clear, concise language
- Include code examples
- Add architecture diagrams where helpful
- Provide troubleshooting sections
- Keep formatting consistent

## Questions?

For questions about contributing to the official CiscoDevNet repository:
- Check the [CiscoDevNet repository](https://github.com/CiscoDevNet/webex-contact-center-ai-sample-code)
- Review existing PRs and issues
- Follow Cisco's contribution guidelines

## Current Status

**Repository Status:** ✅ Initialized with git
**Upstream Remote:** ✅ Configured (CiscoDevNet/webex-contact-center-ai-sample-code)
**Origin Remote:** ⏳ Pending (add when you fork the repository)
**Initial Commit:** ✅ Complete (37 files, 6,976 lines)

**Next Steps:**
1. Fork the CiscoDevNet repository on GitHub
2. Add your fork as the `origin` remote
3. Create feature branches for specific contributions
4. Submit pull requests to contribute back
