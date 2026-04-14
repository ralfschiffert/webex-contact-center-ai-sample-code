package com.cisco.wcc.ccai.client;

import com.cisco.wcc.ccai.v1.InsightsServingRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Main class demonstrating usage of the StreamingInsightClient
 */
public class StreamingInsightClientMain {
    
    private static final Logger logger = LoggerFactory.getLogger(StreamingInsightClientMain.class);
    private static final Pattern UUID_PATTERN = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    
    public static void main(String[] args) {
        if (args.length < 5) {
            printUsage();
            System.exit(1);
        }
        
        // Parse command line arguments
        // Order: host  port  token  orgId  agentId
        String serverHost = args[0];
        int serverPort = Integer.parseInt(args[1]);
        String accessToken = args[2];
        String orgId = args[3];
        String agentId = args[4];
        
        // Validate UUIDs — catch argument order mistakes early
        if (!isValidUuid(orgId)) {
            System.err.println("\n⚠️  ERROR: Organization ID does not look like a UUID: " + truncate(orgId, 40));
            System.err.println("   Expected format: 05ba0660-6b05-48b0-9185-7343434c0784");
            System.err.println("   Argument order: host port token orgId agentId");
            System.err.println("   Tip: Make sure there are no spaces after \\ in line continuations\n");
            System.exit(1);
        }
        if (!isValidUuid(agentId)) {
            System.err.println("\n⚠️  ERROR: Agent ID does not look like a UUID: " + truncate(agentId, 40));
            System.err.println("   Expected format: 3666b2a0-9fa9-4d8e-a1c0-87350d4a2c13");
            System.err.println("   Argument order: host port token orgId agentId\n");
            System.exit(1);
        }
        
        System.out.println("=== Webex Contact Center AI Streaming Insight Client ===");
        System.out.printf("Connecting to: %s:%d%n", serverHost, serverPort);
        System.out.printf("With token: %s%n", truncate(accessToken, 20));
        System.out.printf("Organization ID: %s%n", orgId);
        System.out.printf("Agent ID: %s%n", agentId);
        
        // Create client configuration
        StreamingInsightClientConfig config = StreamingInsightClientConfig.newBuilder()
                .setServerHost(serverHost)
                .setServerPort(serverPort)
                .setUseTls(serverPort == 443) // Use TLS for standard HTTPS port
                .setAccessToken(accessToken)
                .setOrgId(orgId)
                .setAgentId(agentId)
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
        
        String orgId = config.getOrgId();
        String agentId = config.getAgentId();
        System.out.printf("Organization ID: %s%n", orgId);
        System.out.printf("Agent ID: %s%n", agentId);
        
        if (conversationId.isEmpty()) {
            System.out.println("Conversation ID is required!");
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
        
        String orgId = config.getOrgId();
        System.out.printf("Organization ID: %s%n", orgId);
        
        if (conversationId.isEmpty()) {
            System.out.println("Conversation ID is required!");
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
        
        String orgId = config.getOrgId();
        System.out.printf("Organization ID: %s%n", orgId);
        
        if (interactionId.isEmpty()) {
            System.out.println("Interaction ID is required!");
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
        String demoOrgId = config.getOrgId();
        String demoAgentId = config.getAgentId();
        
        System.out.printf("Organization ID: %s%n", demoOrgId);
        System.out.printf("Agent ID: %s%n", demoAgentId);
        
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
    
    private static boolean isValidUuid(String value) {
        return value != null && UUID_PATTERN.matcher(value.trim()).matches();
    }
    
    private static String truncate(String value, int maxLength) {
        if (value == null) return "(not set)";
        if (value.length() <= maxLength) return value;
        return value.substring(0, maxLength) + "...";
    }
    
    private static String getArgOrDefault(String[] args, int index, String defaultValue) {
        return args.length > index ? args[index] : defaultValue;
    }
    
    private static void printUsage() {
        System.out.println("Usage: java -jar streaming-insight-client.jar <host> <port> <access_token> <orgId> <agentId>");
        System.out.println();
        System.out.println("  host:          Server hostname (e.g., serving-api-streaming.wxcc-us1.cisco.com)");
        System.out.println("  port:          Server port (443 for TLS)");
        System.out.println("  access_token:  Agent access token (JWT)");
        System.out.println("  orgId:         Organization ID (UUID from Control Hub)");
        System.out.println("  agentId:       Agent ID (UUID)");
        System.out.println();
        System.out.println("Example:");
        System.out.println("  java -jar streaming-insight-client.jar \\");
        System.out.println("    serving-api-streaming.wxcc-us1.cisco.com \\");
        System.out.println("    443 \\");
        System.out.println("    eyJhbGci... \\");
        System.out.println("    05ba0660-6b05-48b0-9185-7343434c0784 \\");
        System.out.println("    3666b2a0-9fa9-4d8e-a1c0-87350d4a2c13");
    }
}
