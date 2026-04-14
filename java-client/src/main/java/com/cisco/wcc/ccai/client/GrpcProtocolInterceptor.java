package com.cisco.wcc.ccai.client;

import com.cisco.wcc.ccai.v1.InsightServingResponse;
import com.cisco.wcc.ccai.v1.StreamingInsightServingResponse;
import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * gRPC client interceptor that logs the protocol exchange between client and server.
 * <p>
 * Logs at INFO level under the logger name {@code com.cisco.wcc.ccai.client.protocol}.
 * This can be turned on/off via logback.xml configuration:
 * <pre>
 *   &lt;logger name="com.cisco.wcc.ccai.client.protocol" level="INFO"/&gt;   &lt;!-- ON --&gt;
 *   &lt;logger name="com.cisco.wcc.ccai.client.protocol" level="OFF"/&gt;    &lt;!-- OFF --&gt;
 * </pre>
 */
public class GrpcProtocolInterceptor implements ClientInterceptor {

    private static final Logger protocolLogger = LoggerFactory.getLogger("com.cisco.wcc.ccai.client.protocol");

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {

        final String methodName = method.getFullMethodName();

        if (!protocolLogger.isInfoEnabled()) {
            return next.newCall(method, callOptions);
        }

        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
                next.newCall(method, callOptions)) {

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                protocolLogger.info("[gRPC CALL] --> {} (type: {})", methodName, method.getType());

                Listener<RespT> loggingListener = new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {

                    @Override
                    public void onHeaders(Metadata headers) {
                        protocolLogger.info("[gRPC HEADERS] <-- {} headers received", methodName);
                        super.onHeaders(headers);
                    }

                    @Override
                    public void onMessage(RespT message) {
                        logResponseMessage(methodName, message);
                        super.onMessage(message);
                    }

                    @Override
                    public void onClose(Status status, Metadata trailers) {
                        if (status.isOk()) {
                            protocolLogger.info("[gRPC CLOSE] <-- {} completed OK", methodName);
                        } else {
                            protocolLogger.info("[gRPC CLOSE] <-- {} closed with status {} : {}",
                                    methodName, status.getCode(), status.getDescription());
                        }
                        super.onClose(status, trailers);
                    }
                };

                super.start(loggingListener, headers);
            }

            @Override
            public void sendMessage(ReqT message) {
                logRequestMessage(methodName, message);
                super.sendMessage(message);
            }
        };
    }

    private <ReqT> void logRequestMessage(String methodName, ReqT message) {
        if (message == null) return;

        String className = message.getClass().getSimpleName();
        // Log a high-level summary, not the full message (which may contain large payloads)
        String summary = summarizeMessage(message);
        protocolLogger.info("[gRPC REQUEST] --> {} | {} | {}", methodName, className, summary);
    }

    private <RespT> void logResponseMessage(String methodName, RespT message) {
        if (message == null) return;

        String className = message.getClass().getSimpleName();
        String summary = summarizeMessage(message);
        protocolLogger.info("[gRPC RESPONSE] <-- {} | {} | {}", methodName, className, summary);
    }

    /**
     * Produce a human-readable summary of the gRPC message.
     * Extracts key fields from known message types for readability.
     */
    private String summarizeMessage(Object message) {
        try {
            if (message instanceof StreamingInsightServingResponse) {
                StreamingInsightServingResponse resp = (StreamingInsightServingResponse) message;
                if (resp.hasInsightServingResponse()) {
                    InsightServingResponse insight = resp.getInsightServingResponse();
                    return String.format("conversationId=%s, role=%s, type=%s, isFinal=%s, provider=%s",
                            insight.getConversationId(),
                            insight.getRole(),
                            insight.getInsightType(),
                            insight.getIsFinal(),
                            insight.getInsightProvider());
                }
                return message.getClass().getSimpleName();
            }

            // For all other protobuf messages, use a truncated toString
            String full = message.toString();
            if (full.length() > 300) {
                return full.substring(0, 300) + "...(truncated)";
            }
            return full;

        } catch (Exception e) {
            return message.getClass().getSimpleName() + " (summary unavailable)";
        }
    }
}
