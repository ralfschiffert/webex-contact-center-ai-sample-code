# gRPC Error Fix Summary

## Original Error
```
java.lang.IllegalStateException: Could not find policy 'pick_first'. 
Make sure its implementation is either registered to LoadBalancerRegistry 
or included in META-INF/services/io.grpc.LoadBalancerProvider from your jar files.
```

## Root Cause
This error occurs when creating "fat" JARs (JARs containing all dependencies) because:
1. Multiple gRPC library JARs contain service provider files in `META-INF/services/`
2. When building a fat JAR, these service files get overwritten instead of merged
3. The gRPC load balancer providers are lost, causing the `pick_first` policy to be unavailable

## Solution Implemented

### 1. Updated build.gradle Dependencies
Added the missing gRPC core dependency:
```gradle
dependencies {
    implementation "io.grpc:grpc-netty-shaded:${grpcVersion}"
    implementation "io.grpc:grpc-protobuf:${grpcVersion}"
    implementation "io.grpc:grpc-stub:${grpcVersion}"
    implementation "io.grpc:grpc-services:${grpcVersion}"
    implementation "io.grpc:grpc-core:${grpcVersion}"  // ← Added this
    // ... other dependencies
}
```

### 2. Used Shadow Plugin for Proper JAR Packaging
Replaced the manual fat JAR creation with the Shadow plugin which properly merges service files:

```gradle
plugins {
    id 'java'
    id 'com.google.protobuf' version '0.9.4'
    id 'application'
    id 'com.github.johnrengelman.shadow' version '8.1.1'  // ← Added this
}

shadowJar {
    archiveClassifier.set('')
    manifest {
        attributes('Main-Class': 'com.cisco.wcc.ccai.client.StreamingInsightClientMain')
    }
    mergeServiceFiles()  // ← This is the key fix - merges META-INF/services files
    exclude 'META-INF/*.SF'
    exclude 'META-INF/*.DSA'
    exclude 'META-INF/*.RSA'
}

// Task dependencies
build.dependsOn shadowJar
jar.enabled = false
distZip.dependsOn shadowJar
distTar.dependsOn shadowJar
startScripts.dependsOn shadowJar
```

### 3. Fixed TLS Configuration
The secondary error was a TLS mismatch. Fixed by automatically enabling TLS for port 443:
```java
.setUseTls(serverPort == 443) // Use TLS for standard HTTPS port
```

## Results
- ✅ **gRPC Load Balancer Error**: Completely resolved
- ✅ **Service File Merging**: Working properly with Shadow plugin
- ✅ **TLS Connection**: Now connects properly to HTTPS endpoints
- ✅ **Transcript Options Feature**: Working as expected
- ✅ **All Menu Functions**: Working correctly

## Key Takeaway
When creating fat JARs for gRPC applications, always use a tool like the Gradle Shadow plugin that properly merges service provider files rather than simple file concatenation. The `mergeServiceFiles()` function is essential for gRPC applications.

## Testing
The client now works correctly with:
- Token-based authentication
- Organization ID from command line
- Interactive transcript options menu
- Proper TLS connections to production servers
- All streaming and one-time insight functionalities

The original `pick_first` load balancer error has been completely eliminated.
