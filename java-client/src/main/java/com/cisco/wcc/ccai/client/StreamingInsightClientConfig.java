package com.cisco.wcc.ccai.client;

/**
 * Configuration class for the Streaming Insight Client
 */
public class StreamingInsightClientConfig {
    
    private final String serverHost;
    private final int serverPort;
    private final boolean useTls;
    private final String accessToken;
    private final String orgId;
    private final long maxInboundMessageSize;
    private final long keepAliveTimeoutMs;
    private final long keepAliveIntervalMs;
    
    private StreamingInsightClientConfig(Builder builder) {
        this.serverHost = builder.serverHost;
        this.serverPort = builder.serverPort;
        this.useTls = builder.useTls;
        this.accessToken = builder.accessToken;
        this.orgId = builder.orgId;
        this.maxInboundMessageSize = builder.maxInboundMessageSize;
        this.keepAliveTimeoutMs = builder.keepAliveTimeoutMs;
        this.keepAliveIntervalMs = builder.keepAliveIntervalMs;
    }
    
    public String getServerHost() { return serverHost; }
    public int getServerPort() { return serverPort; }
    public boolean isUseTls() { return useTls; }
    public String getAccessToken() { return accessToken; }
    public String getOrgId() { return orgId; }
    public long getMaxInboundMessageSize() { return maxInboundMessageSize; }
    public long getKeepAliveTimeoutMs() { return keepAliveTimeoutMs; }
    public long getKeepAliveIntervalMs() { return keepAliveIntervalMs; }
    
    public static Builder newBuilder() {
        return new Builder();
    }
    
    public static class Builder {
        private String serverHost = "localhost";
        private int serverPort = 9090;
        private boolean useTls = false;
        private String accessToken;
        private String orgId;
        private long maxInboundMessageSize = 4 * 1024 * 1024; // 4MB
        private long keepAliveTimeoutMs = 30000; // 30 seconds
        private long keepAliveIntervalMs = 10000; // 10 seconds
        
        public Builder setServerHost(String serverHost) {
            this.serverHost = serverHost;
            return this;
        }
        
        public Builder setServerPort(int serverPort) {
            this.serverPort = serverPort;
            return this;
        }
        
        public Builder setUseTls(boolean useTls) {
            this.useTls = useTls;
            return this;
        }
        
        public Builder setAccessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }
        
        public Builder setOrgId(String orgId) {
            this.orgId = orgId;
            return this;
        }
        
        public Builder setMaxInboundMessageSize(long maxInboundMessageSize) {
            this.maxInboundMessageSize = maxInboundMessageSize;
            return this;
        }
        
        public Builder setKeepAliveTimeoutMs(long keepAliveTimeoutMs) {
            this.keepAliveTimeoutMs = keepAliveTimeoutMs;
            return this;
        }
        
        public Builder setKeepAliveIntervalMs(long keepAliveIntervalMs) {
            this.keepAliveIntervalMs = keepAliveIntervalMs;
            return this;
        }
        
        public StreamingInsightClientConfig build() {
            return new StreamingInsightClientConfig(this);
        }
    }
}
