package com.cisco.wcc.ccai.client;

import com.cisco.wcc.ccai.v1.*;
import com.cisco.wcc.ccai.v1.Messages.Message;
import com.cisco.wcc.ccai.v1.Recognize.SpeechRecognitionAlternative;
import com.cisco.wcc.ccai.v1.Recognize.StreamingRecognitionResult;
import com.cisco.wcc.ccai.v1.Suggestions.AgentAnswer;
import com.cisco.wcc.ccai.v1.Suggestions.Answer;
import com.cisco.wcc.ccai.v1.Virtualagent.NLU;
import com.cisco.wcc.ccai.v1.Virtualagent.Entity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for handling streaming insight responses
 */
public class ResponseHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(ResponseHandler.class);
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);
    
    /**
     * Create a simple console response handler that logs all responses
     * @return Consumer that logs responses to console
     */
    public static java.util.function.Consumer<StreamingInsightServingResponse> createConsoleHandler() {
        return response -> {
            try {
                InsightServingResponse insight = response.getInsightServingResponse();
                
                System.out.printf("%n=== New Insight Received ===%n");
                System.out.printf("Conversation ID: %s%n", insight.getConversationId());
                System.out.printf("Role: %s%n", insight.getRole());
                System.out.printf("Insight Type: %s%n", insight.getInsightType());
                System.out.printf("Provider: %s%n", insight.getInsightProvider());
                System.out.printf("Is Final: %s%n", insight.getIsFinal());
                System.out.printf("Publish Timestamp: %d%n", insight.getPublishTimestamp());
                
                if (insight.hasResponseContent()) {
                    handleResponseContent(insight.getResponseContent());
                }
                
                System.out.println("===============================");
                
            } catch (Exception e) {
                logger.error("Error processing response", e);
            }
        };
    }
    
    /**
     * Create a JSON response handler that outputs responses as JSON
     * @return Consumer that outputs JSON to console
     */
    public static java.util.function.Consumer<StreamingInsightServingResponse> createJsonHandler() {
        return response -> {
            try {
                // Convert protobuf to JSON for pretty printing
                String jsonOutput = convertToJsonString(response);
                System.out.println("Received insight:");
                System.out.println(jsonOutput);
                System.out.println("---");
            } catch (Exception e) {
                logger.error("Error converting response to JSON", e);
            }
        };
    }
    
    /**
     * Create a specific transcript handler that only processes transcription insights
     * @return Consumer for transcript responses
     */
    public static java.util.function.Consumer<StreamingInsightServingResponse> createTranscriptHandler() {
        return response -> {
            try {
                InsightServingResponse insight = response.getInsightServingResponse();
                
                if (insight.getInsightType() == InsightServingResponse.ServiceType.TRANSCRIPTION) {
                    System.out.printf("%n[TRANSCRIPT] %s: ", insight.getRole());
                    
                    if (insight.hasResponseContent() && 
                        insight.getResponseContent().hasRecognitionResult()) {
                        
                        StreamingRecognitionResult result = insight.getResponseContent().getRecognitionResult();
                        if (!result.getAlternativesList().isEmpty()) {
                            String transcript = result.getAlternatives(0).getTranscript();
                            String finalStatus = result.getIsFinal() ? "[FINAL]" : "[INTERIM]";
                            System.out.printf("%s %s%n", transcript, finalStatus);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error processing transcript", e);
            }
        };
    }
    
    private static void handleResponseContent(ResponseContent content) {
        switch (content.getStreamResponseContentCase()) {
            case RECOGNITIONRESULT:
                handleTranscriptionResult(content.getRecognitionResult());
                break;
            case AGENTANSWERRESULT:
                handleAgentAnswerResult(content.getAgentAnswerResult());
                break;
            case MESSAGERESULT:
                handleMessageResult(content.getMessageResult());
                break;
            case VIRTUALAGENTRESULT:
                handleVirtualAgentResult(content.getVirtualAgentResult());
                break;
            case RAWCONTENT:
                System.out.printf("Raw Content: %s%n", content.getRawContent());
                break;
            default:
                System.out.println("Unknown response content type");
        }
    }
    
    private static void handleTranscriptionResult(StreamingRecognitionResult result) {
        System.out.printf("Transcription Result:%n");
        System.out.printf("  Is Final: %s%n", result.getIsFinal());
        System.out.printf("  Language: %s%n", result.getLanguageCode());
        
        if (!result.getAlternativesList().isEmpty()) {
            SpeechRecognitionAlternative alt = result.getAlternatives(0);
            System.out.printf("  Transcript: %s%n", alt.getTranscript());
            System.out.printf("  Confidence: %.2f%n", alt.getConfidence());
        }
    }
    
    private static void handleAgentAnswerResult(AgentAnswer agentAnswer) {
        System.out.printf("Agent Answer Result:%n");
        System.out.printf("  Number of answers: %d%n", agentAnswer.getAnswersCount());
        
        for (int i = 0; i < agentAnswer.getAnswersCount(); i++) {
            Answer answer = agentAnswer.getAnswers(i);
            System.out.printf("  Answer %d:%n", i + 1);
            System.out.printf("    Title: %s%n", answer.getTitle());
            System.out.printf("    Description: %s%n", answer.getDescription());
            System.out.printf("    Confidence: %.2f%n", answer.getConfidence());
        }
    }
    
    private static void handleMessageResult(Message message) {
        System.out.printf("Message Result:%n");
        System.out.printf("  ID: %s%n", message.getId());
        System.out.printf("  Content: %s%n", message.getContent());
        System.out.printf("  Sender: %s%n", message.getSenderName());
        System.out.printf("  Type: %s%n", message.getType());
    }
    
    private static void handleVirtualAgentResult(NLU nlu) {
        System.out.printf("Virtual Agent Result:%n");
        System.out.printf("  Intent: %s%n", nlu.getIntent());
        System.out.printf("  Confidence: %.2f%n", nlu.getConfidence());
        System.out.printf("  Query: %s%n", nlu.getQueryText());
        System.out.printf("  Fulfillment: %s%n", nlu.getFulfillmentText());
        
        if (!nlu.getEntitiesList().isEmpty()) {
            System.out.println("  Entities:");
            for (Entity entity : nlu.getEntitiesList()) {
                System.out.printf("    %s: %s (%.2f)%n", 
                    entity.getType(), entity.getValue(), entity.getConfidence());
            }
        }
    }
    
    private static String convertToJsonString(Object object) {
        try {
            // This is a simplified JSON conversion
            // In practice, you might want to use protobuf-util for better JSON conversion
            return object.toString(); // Fallback to toString for protobuf objects
        } catch (Exception e) {
            logger.warn("Could not convert to JSON, using toString", e);
            return object.toString();
        }
    }
    
    /**
     * Create a simple error handler
     * @return Consumer for handling errors
     */
    public static java.util.function.Consumer<Throwable> createErrorHandler() {
        return error -> {
            logger.error("Streaming insight error occurred", error);
            System.err.printf("Error: %s%n", error.getMessage());
        };
    }
}
