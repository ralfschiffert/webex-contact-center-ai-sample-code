package com.cisco.wcc.ccai.client;

import com.cisco.wcc.ccai.v1.InsightsServingRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Main class demonstrating usage of the StreamingInsightClient
 */
public class StreamingInsightClientMain {
    
    private static final Logger logger = LoggerFactory.getLogger(StreamingInsightClientMain.class);
    
    public static void main(String[] args) {
        // Parse command line arguments or use defaults
        // token  orgId  server  port
        String accessToken = getArgOrDefault(args, 0, null);
        String orgId = getArgOrDefault(args, 1, null);
        String serverHost = getArgOrDefault(args, 2, "serving-api-streaming.wxcc-us1.cisco.com");
        int serverPort = Integer.parseInt(getArgOrDefault(args, 3, "443"));
        
        
        System.out.println("=== Webex Contact Center AI Streaming Insight Client ===");
        System.out.printf("Connecting to: %s:%d%n", serverHost, serverPort);
        System.out.printf("With token %s%n", accessToken);
        System.out.printf("Organization ID: %s%n", orgId);
        
        // Create client configuration
        StreamingInsightClientConfig config = StreamingInsightClientConfig.newBuilder()
                .setServerHost(serverHost)
                .setServerPort(serverPort)
                .setUseTls(serverPort == 443) // Use TLS for standard HTTPS port
                .setAccessToken(accessToken)
                .setOrgId(orgId)
                .build();
        
        try (StreamingInsightClient client = new StreamingInsightClient(config)) {
            
            // Run interactive demo
            runInteractiveDemo(client, config);
            
        } catch (Exception e) {
            logger.error("Error running client", e);
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
    
    private static void runInteractiveDemo(StreamingInsightClient client, StreamingInsightClientConfig config) {
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            System.out.println("\n=== Menu ===");
            System.out.println("1. Start streaming insights");
            System.out.println("2. Get one-time insights");
            System.out.println("3. Get insights by interaction ID");
            System.out.println("4. Streaming demo (with sample data)");
            System.out.println("5. Exit");
            System.out.print("Select an option (1-5): ");
            
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    startStreamingInsights(client, scanner, config);
                    break;
                case "2":
                    getOneTimeInsights(client, scanner, config);
                    break;
                case "3":
                    getInsightsByInteractionId(client, scanner, config);
                    break;
                case "4":
                    runStreamingDemo(client, config);
                    break;
                case "5":
                    System.out.println("Exiting...");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    private static void startStreamingInsights(StreamingInsightClient client, Scanner scanner, StreamingInsightClientConfig config) {
        System.out.println("\n=== Start Streaming Insights ===");
        
        System.out.print("Enter conversation ID: ");
        String conversationId = scanner.nextLine().trim();
        
        String orgId;
        if (config.getOrgId() != null && !config.getOrgId().isEmpty()) {
            orgId = config.getOrgId();
            System.out.printf("Using organization ID from config: %s%n", orgId);
        } else {
            System.out.print("Enter organization ID: ");
            orgId = scanner.nextLine().trim();
        }
        
        System.out.print("Enter agent ID: ");
        String agentId = scanner.nextLine().trim();
        
        if (conversationId.isEmpty() || orgId.isEmpty() || agentId.isEmpty()) {
            System.out.println("All fields are required!");
            return;
        }
        
        System.out.println("\nSelect transcript options:");
        System.out.println("1. Real-time transcripts only");
        System.out.println("2. Historical transcripts only");
        System.out.println("3. Both real-time and historical transcripts");
        System.out.print("Select transcript option (1-3): ");
        
        String transcriptChoice = scanner.nextLine().trim();
        boolean realTimeTranscripts = false;
        boolean historicalTranscripts = false;
        
        switch (transcriptChoice) {
            case "1":
                realTimeTranscripts = true;
                System.out.println("Selected: Real-time transcripts only");
                break;
            case "2":
                historicalTranscripts = true;
                System.out.println("Selected: Historical transcripts only");
                break;
            case "3":
                realTimeTranscripts = true;
                historicalTranscripts = true;
                System.out.println("Selected: Both real-time and historical transcripts");
                break;
            default:
                System.out.println("Invalid choice, using real-time transcripts only");
                realTimeTranscripts = true;
        }
        
        System.out.println("\nChoose response handler:");
        System.out.println("1. Console handler (detailed output)");
        System.out.println("2. Transcript handler (transcripts only)");
        System.out.println("3. JSON handler (raw JSON output)");
        System.out.print("Select handler (1-3): ");
        
        String handlerChoice = scanner.nextLine().trim();
        java.util.function.Consumer<com.cisco.wcc.ccai.v1.StreamingInsightServingResponse> responseHandler;
        
        switch (handlerChoice) {
            case "1":
                responseHandler = ResponseHandler.createConsoleHandler();
                break;
            case "2":
                responseHandler = ResponseHandler.createTranscriptHandler();
                break;
            case "3":
                responseHandler = ResponseHandler.createJsonHandler();
                break;
            default:
                System.out.println("Invalid choice, using console handler");
                responseHandler = ResponseHandler.createConsoleHandler();
        }
        
        System.out.println("\nStarting streaming insights...");
        System.out.println("Press Enter to stop streaming\n");
        
        try {
            StreamingInsightClient.StreamingInsightSession session = client.startStreamingInsights(
                conversationId,
                orgId,
                agentId,
                realTimeTranscripts,
                historicalTranscripts,
                responseHandler,
                ResponseHandler.createErrorHandler()
            );
            
            // Wait for user input to stop
            scanner.nextLine();
            
            System.out.println("Stopping streaming insights...");
            session.cancel();
            
            // Wait a bit for cleanup
            session.awaitCompletion(5, TimeUnit.SECONDS);
            
        } catch (Exception e) {
            logger.error("Error during streaming", e);
            System.err.println("Streaming error: " + e.getMessage());
        }
    }
    
    private static void getOneTimeInsights(StreamingInsightClient client, Scanner scanner, StreamingInsightClientConfig config) {
        System.out.println("\n=== Get One-Time Insights ===");
        
        System.out.print("Enter conversation ID: ");
        String conversationId = scanner.nextLine().trim();
        
        String orgId;
        if (config.getOrgId() != null && !config.getOrgId().isEmpty()) {
            orgId = config.getOrgId();
            System.out.printf("Using organization ID from config: %s%n", orgId);
        } else {
            System.out.print("Enter organization ID: ");
            orgId = scanner.nextLine().trim();
        }
        
        if (conversationId.isEmpty() || orgId.isEmpty()) {
            System.out.println("Conversation ID and Organization ID are required!");
            return;
        }
        
        System.out.println("Select insight type:");
        System.out.println("1. Transcription");
        System.out.println("2. Agent Answers");
        System.out.println("3. Virtual Agent");
        System.out.println("4. Messages");
        System.out.print("Select type (1-4): ");
        
        String typeChoice = scanner.nextLine().trim();
        InsightsServingRequest.InsightType insightType;
        
        switch (typeChoice) {
            case "1":
                insightType = InsightsServingRequest.InsightType.TRANSCRIPTION;
                break;
            case "2":
                insightType = InsightsServingRequest.InsightType.AGENT_ANSWERS;
                break;
            case "3":
                insightType = InsightsServingRequest.InsightType.VIRTUAL_AGENT;
                break;
            case "4":
                insightType = InsightsServingRequest.InsightType.MESSAGE;
                break;
            default:
                System.out.println("Invalid choice, using TRANSCRIPTION");
                insightType = InsightsServingRequest.InsightType.TRANSCRIPTION;
        }
        
        try {
            System.out.println("\nFetching insights...");
            var response = client.getInsights(conversationId, orgId, insightType);
            
            System.out.println("\n=== Insights Response ===");
            System.out.printf("Conversation ID: %s%n", response.getConversationId());
            System.out.printf("Organization ID: %s%n", response.getOrgId());
            System.out.printf("Insight Provider: %s%n", response.getInsightProvider());
            System.out.printf("Response Content Count: %d%n", response.getResponseContentCount());
            
            for (int i = 0; i < response.getResponseContentCount(); i++) {
                System.out.printf("\nContent %d: %s%n", i + 1, response.getResponseContent(i));
            }
            
        } catch (Exception e) {
            logger.error("Error getting insights", e);
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    private static void getInsightsByInteractionId(StreamingInsightClient client, Scanner scanner, StreamingInsightClientConfig config) {
        System.out.println("\n=== Get Insights by Interaction ID ===");
        
        System.out.print("Enter interaction ID (conversation/message ID): ");
        String interactionId = scanner.nextLine().trim();
        
        String orgId;
        if (config.getOrgId() != null && !config.getOrgId().isEmpty()) {
            orgId = config.getOrgId();
            System.out.printf("Using organization ID from config: %s%n", orgId);
        } else {
            System.out.print("Enter organization ID: ");
            orgId = scanner.nextLine().trim();
        }
        
        if (interactionId.isEmpty() || orgId.isEmpty()) {
            System.out.println("Interaction ID and Organization ID are both required!");
            return;
        }
        
        System.out.println("Select insight type:");
        System.out.println("1. Transcription");
        System.out.println("2. Agent Answers");
        System.out.println("3. Virtual Agent");
        System.out.println("4. Messages");
        System.out.print("Select type (1-4): ");
        
        String typeChoice = scanner.nextLine().trim();
        InsightsServingRequest.InsightType insightType;
        
        switch (typeChoice) {
            case "1":
                insightType = InsightsServingRequest.InsightType.TRANSCRIPTION;
                break;
            case "2":
                insightType = InsightsServingRequest.InsightType.AGENT_ANSWERS;
                break;
            case "3":
                insightType = InsightsServingRequest.InsightType.VIRTUAL_AGENT;
                break;
            case "4":
                insightType = InsightsServingRequest.InsightType.MESSAGE;
                break;
            default:
                System.out.println("Invalid choice, using TRANSCRIPTION");
                insightType = InsightsServingRequest.InsightType.TRANSCRIPTION;
        }
        
        try {
            System.out.println("\nFetching insights for interaction ID...");
            var response = client.getInsightsByInteractionId(interactionId, interactionId, orgId, insightType);
            
            System.out.println("\n=== Insights Response ===");
            System.out.printf("Conversation ID: %s%n", response.getConversationId());
            System.out.printf("Interaction ID (Message ID): %s%n", response.getMessageId());
            System.out.printf("Organization ID: %s%n", response.getOrgId());
            System.out.printf("Insight Provider: %s%n", response.getInsightProvider());
            System.out.printf("Response Content Count: %d%n", response.getResponseContentCount());
            
            for (int i = 0; i < response.getResponseContentCount(); i++) {
                System.out.printf("\nContent %d: %s%n", i + 1, response.getResponseContent(i));
            }
            
        } catch (Exception e) {
            logger.error("Error getting insights by interaction ID", e);
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    private static void runStreamingDemo(StreamingInsightClient client, StreamingInsightClientConfig config) {
        System.out.println("\n=== Streaming Demo ===");
        System.out.println("Running demo with sample data...");
        System.out.println("This will connect to the server and listen for insights.");
        System.out.println("Press Enter to stop the demo\n");
        
        String demoConversationId = "demo-conversation-" + System.currentTimeMillis();
        String demoOrgId = (config.getOrgId() != null && !config.getOrgId().isEmpty()) ? config.getOrgId() : "demo-org-123";
        String demoAgentId = "demo-agent-456";
        
        if (config.getOrgId() != null && !config.getOrgId().isEmpty()) {
            System.out.printf("Using organization ID from config: %s%n", demoOrgId);
        } else {
            System.out.printf("Using demo organization ID: %s%n", demoOrgId);
        }
        
        try {
            StreamingInsightClient.StreamingInsightSession session = client.startStreamingInsights(
                demoConversationId,
                demoOrgId,
                demoAgentId,
                ResponseHandler.createConsoleHandler(),
                ResponseHandler.createErrorHandler()
            );
            
            System.out.printf("Demo started for conversation: %s%n", demoConversationId);
            System.out.println("Waiting for insights... (Press Enter to stop)");
            
            // Wait for user input
            Scanner scanner = new Scanner(System.in);
            scanner.nextLine();
            
            System.out.println("\nStopping demo...");
            session.cancel();
            
            // Wait for completion
            if (session.awaitCompletion(5, TimeUnit.SECONDS)) {
                System.out.println("Demo stopped successfully.");
            } else {
                System.out.println("Demo stopped (timeout).");
            }
            
        } catch (Exception e) {
            logger.error("Error during demo", e);
            System.err.println("Demo error: " + e.getMessage());
        }
    }
    
    private static String getArgOrDefault(String[] args, int index, String defaultValue) {
        return args.length > index ? args[index] : defaultValue;
    }
    
    private static void printUsage() {
        System.out.println("Usage: java -jar streaming-insight-client.jar [access_token] [orgId] [host] [port]");
        System.out.println("  access_token: Bearer token for authentication (optional)");
        System.out.println("  orgId: Organization ID (optional - if provided, will be used for all requests)");
        System.out.println("  host: Server hostname (default: serving-api-streaming.wxcc-us1.cisco.com)");
        System.out.println("  port: Server port (default: 443)");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java -jar streaming-insight-client.jar your-token-here");
        System.out.println("  java -jar streaming-insight-client.jar your-token-here your-org-id");
        System.out.println("  java -jar streaming-insight-client.jar your-token-here your-org-id api.wxcc.ai 443");
    }
}
