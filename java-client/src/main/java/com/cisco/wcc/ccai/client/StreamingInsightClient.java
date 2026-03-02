package com.cisco.wcc.ccai.client;

import com.cisco.wcc.ccai.v1.AiInsightGrpc;
import com.cisco.wcc.ccai.v1.StreamingInsightServingRequest;
import com.cisco.wcc.ccai.v1.StreamingInsightServingResponse;
import com.cisco.wcc.ccai.v1.InsightServingRequest;
import com.cisco.wcc.ccai.v1.AgentDetails;
import com.cisco.wcc.ccai.v1.InsightsServingRequest;
import com.cisco.wcc.ccai.v1.InsightsServingResponse;

import io.grpc.*;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * gRPC client for streaming AI insights from Webex Contact Center
 */
public class StreamingInsightClient implements AutoCloseable {
    
    private static final Logger logger = LoggerFactory.getLogger(StreamingInsightClient.class);
    
    private final StreamingInsightClientConfig config;
    private final ManagedChannel channel;
    private final AiInsightGrpc.AiInsightStub asyncStub;
    private final AtomicBoolean isShutdown = new AtomicBoolean(false);
    
    /**
     * Create a new StreamingInsightClient
     * @param config Client configuration
     */
    public StreamingInsightClient(StreamingInsightClientConfig config) {
        this.config = config;
        this.channel = createChannel();
        this.asyncStub = AiInsightGrpc.newStub(channel);
        
        logger.info("StreamingInsightClient initialized with server {}:{}", 
            config.getServerHost(), config.getServerPort());
    }
    
    private ManagedChannel createChannel() {
        NettyChannelBuilder channelBuilder = NettyChannelBuilder
            .forAddress(config.getServerHost(), config.getServerPort())
            .maxInboundMessageSize((int) config.getMaxInboundMessageSize())
            .keepAliveTime(config.getKeepAliveIntervalMs(), TimeUnit.MILLISECONDS)
            .keepAliveTimeout(config.getKeepAliveTimeoutMs(), TimeUnit.MILLISECONDS)
            .keepAliveWithoutCalls(true);
            
        if (config.isUseTls()) {
            channelBuilder.useTransportSecurity();
        } else {
            channelBuilder.usePlaintext();
        }
        
        return channelBuilder.build();
    }
    
    private ClientInterceptor createAuthInterceptor() {
        return new ClientInterceptor() {
            @Override
            public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
                    MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
                return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
                        next.newCall(method, callOptions)) {
                    @Override
                    public void start(Listener<RespT> responseListener, Metadata headers) {
                        if (config.getAccessToken() != null && !config.getAccessToken().isEmpty()) {
                            headers.put(Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER), 
                                "Bearer " + config.getAccessToken());
                        }
                        super.start(responseListener, headers);
                    }
                };
            }
        };
    }
    
    /**
     * Start streaming insights for a conversation
     * @param conversationId The conversation ID to stream insights for
     * @param orgId The organization ID
     * @param agentId The agent ID initiating the request
     * @param responseHandler Handler for streaming responses
     * @param errorHandler Handler for errors
     * @return StreamingInsightSession for controlling the stream
     */
    public StreamingInsightSession startStreamingInsights(
            String conversationId,
            String orgId,
            String agentId,
            Consumer<StreamingInsightServingResponse> responseHandler,
            Consumer<Throwable> errorHandler) {
        
        return startStreamingInsights(
            createDefaultInsightRequest(conversationId, orgId, agentId),
            responseHandler,
            errorHandler
        );
    }
    
    /**
     * Start streaming insights for a conversation with custom transcript options
     * @param conversationId The conversation ID to stream insights for
     * @param orgId The organization ID
     * @param agentId The agent ID initiating the request
     * @param realTimeTranscripts Whether to include real-time transcripts
     * @param historicalTranscripts Whether to include historical transcripts
     * @param responseHandler Handler for streaming responses
     * @param errorHandler Handler for errors
     * @return StreamingInsightSession for controlling the stream
     */
    public StreamingInsightSession startStreamingInsights(
            String conversationId,
            String orgId,
            String agentId,
            boolean realTimeTranscripts,
            boolean historicalTranscripts,
            Consumer<StreamingInsightServingResponse> responseHandler,
            Consumer<Throwable> errorHandler) {
        
        return startStreamingInsights(
            createCustomInsightRequest(conversationId, orgId, agentId, realTimeTranscripts, historicalTranscripts),
            responseHandler,
            errorHandler
        );
    }
    
    /**
     * Start streaming insights with custom request
     * @param request Custom insight serving request
     * @param responseHandler Handler for streaming responses
     * @param errorHandler Handler for errors
     * @return StreamingInsightSession for controlling the stream
     */
    public StreamingInsightSession startStreamingInsights(
            InsightServingRequest request,
            Consumer<StreamingInsightServingResponse> responseHandler,
            Consumer<Throwable> errorHandler) {
        
        if (isShutdown.get()) {
            throw new IllegalStateException("Client is shutdown");
        }
        
        StreamingInsightServingRequest streamingRequest = StreamingInsightServingRequest.newBuilder()
            .setInsightServingRequest(request)
            .build();
            
        CountDownLatch finishedLatch = new CountDownLatch(1);
        AtomicBoolean isActive = new AtomicBoolean(true);
        
        AiInsightGrpc.AiInsightStub stub = config.getAccessToken() != null ? 
            asyncStub.withInterceptors(createAuthInterceptor()) : asyncStub;
            
        StreamObserver<StreamingInsightServingResponse> responseObserver = new StreamObserver<StreamingInsightServingResponse>() {
            @Override
            public void onNext(StreamingInsightServingResponse response) {
                if (isActive.get()) {
                    try {
                        responseHandler.accept(response);
                    } catch (Exception e) {
                        logger.error("Error in response handler", e);
                    }
                }
            }
            
            @Override
            public void onError(Throwable throwable) {
                logger.error("Streaming insights error for conversation {}", 
                    request.getConversationId(), throwable);
                isActive.set(false);
                if (errorHandler != null) {
                    try {
                        errorHandler.accept(throwable);
                    } catch (Exception e) {
                        logger.error("Error in error handler", e);
                    }
                }
                finishedLatch.countDown();
            }
            
            @Override
            public void onCompleted() {
                logger.info("Streaming insights completed for conversation {}", 
                    request.getConversationId());
                isActive.set(false);
                finishedLatch.countDown();
            }
        };
        
        logger.info("Starting streaming insights for conversation {} in org {}", 
            request.getConversationId(), request.getOrgId());
            
        stub.streamingInsightServing(streamingRequest, responseObserver);
        
        return new StreamingInsightSession(isActive, finishedLatch, request.getConversationId());
    }
    
    /**
     * Get insights (one-time request, not streaming)
     * @param conversationId Conversation ID
     * @param orgId Organization ID
     * @param insightType Type of insight to retrieve
     * @return InsightsServingResponse
     */
    public InsightsServingResponse getInsights(String conversationId, String orgId, 
                                             InsightsServingRequest.InsightType insightType) {
        if (isShutdown.get()) {
            throw new IllegalStateException("Client is shutdown");
        }
        
        InsightsServingRequest request = InsightsServingRequest.newBuilder()
            .setConversationId(conversationId)
            .setOrgId(orgId)
            .setInsightType(insightType)
            .build();
            
        AiInsightGrpc.AiInsightBlockingStub blockingStub = AiInsightGrpc.newBlockingStub(channel);
        if (config.getAccessToken() != null) {
            blockingStub = blockingStub.withInterceptors(createAuthInterceptor());
        }
        
        logger.info("Getting insights for conversation {} in org {} with type {}", 
            conversationId, orgId, insightType);
            
        return blockingStub.insightServing(request);
    }
    
    /**
     * Get insights by interaction ID (one-time request, not streaming)
     * Note: conversationId and interactionId are typically the same value
     * @param conversationId Conversation ID
     * @param interactionId Interaction ID (message ID) - typically same as conversationId
     * @param orgId Organization ID
     * @param insightType Type of insight to retrieve
     * @return InsightsServingResponse
     */
    public InsightsServingResponse getInsightsByInteractionId(String conversationId, String interactionId, 
                                                            String orgId, InsightsServingRequest.InsightType insightType) {
        if (isShutdown.get()) {
            throw new IllegalStateException("Client is shutdown");
        }
        
        InsightsServingRequest request = InsightsServingRequest.newBuilder()
            .setConversationId(conversationId)
            .setMessageId(interactionId)
            .setOrgId(orgId)
            .setInsightType(insightType)
            .build();
            
        AiInsightGrpc.AiInsightBlockingStub blockingStub = AiInsightGrpc.newBlockingStub(channel);
        if (config.getAccessToken() != null) {
            blockingStub = blockingStub.withInterceptors(createAuthInterceptor());
        }
        
        logger.info("Getting insights for conversation {} with interaction ID {} in org {} with type {}", 
            conversationId, interactionId, orgId, insightType);
            
        return blockingStub.insightServing(request);
    }
    
    private InsightServingRequest createDefaultInsightRequest(String conversationId, 
                                                            String orgId, String agentId) {
        return InsightServingRequest.newBuilder()
            .setConversationId(conversationId)
            .setOrgId(orgId)
            .setRealTimeTranscripts(true)
            .setHistoricalTranscripts(false)
            .setRealtimeAgentAssist(true)
            .setHistoricalAgentAssist(false)
            .setRealTimeMessage(true)
            .setHistoricalMessage(false)
            .setHistoricalVirtualAgent(false)
            .setAgentDetails(AgentDetails.newBuilder().setAgentId(agentId).build())
            .build();
    }
    
    private InsightServingRequest createCustomInsightRequest(String conversationId, 
                                                           String orgId, String agentId,
                                                           boolean realTimeTranscripts,
                                                           boolean historicalTranscripts) {
        return InsightServingRequest.newBuilder()
            .setConversationId(conversationId)
            .setOrgId(orgId)
            .setRealTimeTranscripts(realTimeTranscripts)
            .setHistoricalTranscripts(historicalTranscripts)
            .setRealtimeAgentAssist(true)
            .setHistoricalAgentAssist(false)
            .setRealTimeMessage(true)
            .setHistoricalMessage(false)
            .setHistoricalVirtualAgent(false)
            .setAgentDetails(AgentDetails.newBuilder().setAgentId(agentId).build())
            .build();
    }
    
    @Override
    public void close() throws Exception {
        if (isShutdown.compareAndSet(false, true)) {
            logger.info("Shutting down StreamingInsightClient...");
            try {
                channel.shutdown();
                if (!channel.awaitTermination(30, TimeUnit.SECONDS)) {
                    logger.warn("Channel did not terminate gracefully, forcing shutdown");
                    channel.shutdownNow();
                }
                logger.info("StreamingInsightClient shutdown complete");
            } catch (InterruptedException e) {
                logger.warn("Interrupted while shutting down channel", e);
                channel.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * Session representing an active streaming insights connection
     */
    public static class StreamingInsightSession {
        private final AtomicBoolean isActive;
        private final CountDownLatch finishedLatch;
        private final String conversationId;
        
        StreamingInsightSession(AtomicBoolean isActive, CountDownLatch finishedLatch, String conversationId) {
            this.isActive = isActive;
            this.finishedLatch = finishedLatch;
            this.conversationId = conversationId;
        }
        
        /**
         * Check if the streaming session is still active
         * @return true if active, false otherwise
         */
        public boolean isActive() {
            return isActive.get();
        }
        
        /**
         * Get the conversation ID for this session
         * @return conversation ID
         */
        public String getConversationId() {
            return conversationId;
        }
        
        /**
         * Cancel the streaming session
         */
        public void cancel() {
            isActive.set(false);
        }
        
        /**
         * Wait for the streaming session to complete
         * @param timeout Maximum time to wait
         * @param unit Time unit for timeout
         * @return true if completed within timeout, false otherwise
         * @throws InterruptedException if interrupted while waiting
         */
        public boolean awaitCompletion(long timeout, TimeUnit unit) throws InterruptedException {
            return finishedLatch.await(timeout, unit);
        }
        
        /**
         * Wait indefinitely for the streaming session to complete
         * @throws InterruptedException if interrupted while waiting
         */
        public void awaitCompletion() throws InterruptedException {
            finishedLatch.await();
        }
    }
}
