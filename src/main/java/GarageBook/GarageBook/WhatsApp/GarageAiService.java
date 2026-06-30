package GarageBook.GarageBook.WhatsApp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import GarageBook.GarageBook.Models.ChatHistory;
import GarageBook.GarageBook.Models.ChatSession;
import GarageBook.GarageBook.Models.Garage;
import GarageBook.GarageBook.Repository.ChatHistoryRepository;
import GarageBook.GarageBook.Repository.ChatSessionRepository;
import GarageBook.GarageBook.Repository.GarageRepository;

@Service
public class GarageAiService {

    private final ChatClient chatClient;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatHistoryRepository chatHistoryRepository;
    private final GarageRepository garageRepository;
    private final WhatsAppNotificationService whatsAppNotificationService;

    public GarageAiService(
            ChatClient.Builder chatClientBuilder,
            ChatSessionRepository chatSessionRepository,
            ChatHistoryRepository chatHistoryRepository,
            GarageRepository garageRepository,
            WhatsAppNotificationService whatsAppNotificationService,
            GarageAiTools garageAiTools) {
        this.chatClient = chatClientBuilder
            .defaultTools(garageAiTools)
            .build();
        this.chatSessionRepository = chatSessionRepository;
        this.chatHistoryRepository = chatHistoryRepository;
        this.garageRepository = garageRepository;
        this.whatsAppNotificationService = whatsAppNotificationService;
    }

    @Async
    public void processIncomingWebhookAsync(String payload) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(payload);
            JsonNode entryNode = root.path("entry").get(0);
            if (entryNode == null) return;
            JsonNode changeNode = entryNode.path("changes").get(0);
            if (changeNode == null) return;
            JsonNode valueNode = changeNode.path("value");
            if (valueNode == null) return;

            JsonNode messagesNode = valueNode.path("messages");
            if (messagesNode == null || !messagesNode.isArray() || messagesNode.size() == 0) {
                return; // Ignore status notifications or other changes
            }

            JsonNode messageNode = messagesNode.get(0);
            String customerPhone = messageNode.path("from").asText();
            String messageType = messageNode.path("type").asText();

            if (!"text".equalsIgnoreCase(messageType)) {
                return;
            }
            String messageText = messageNode.path("text").path("body").asText();

            final String customerName;
            JsonNode contactsNode = valueNode.path("contacts");
            if (contactsNode != null && contactsNode.isArray() && contactsNode.size() > 0) {
                customerName = contactsNode.get(0).path("profile").path("name").asText("Unknown Customer");
            } else {
                customerName = "Unknown Customer";
            }

            List<Garage> garages = garageRepository.findAll();
            if (garages.isEmpty()) {
                System.err.println("No garages found in the database. Cannot route WhatsApp conversation.");
                return;
            }

            // Route to the first garage as default multi-tenant fallback, or resolve by phone number metadata
            Garage garage = garages.get(0);

            // Find or create ChatSession isolated by garageId
            ChatSession session = chatSessionRepository.findByCustomerPhoneNumberAndGarageGarageId(
                customerPhone, garage.getGarageId()
            ).orElseGet(() -> {
                ChatSession newSession = ChatSession.builder()
                    .customerPhoneNumber(customerPhone)
                    .customerName(customerName)
                    .isAiActive(true)
                    .lastMessageTime(LocalDateTime.now())
                    .garage(garage)
                    .build();
                return chatSessionRepository.save(newSession);
            });

            // Save CUSTOMER message log
            ChatHistory customerMsg = ChatHistory.builder()
                .chatSession(session)
                .sender("CUSTOMER")
                .message(messageText)
                .timestamp(LocalDateTime.now())
                .garage(garage)
                .build();
            chatHistoryRepository.save(customerMsg);

            session.setLastMessageTime(LocalDateTime.now());
            chatSessionRepository.save(session);

            // Check manual override bypass
            if (!session.getIsAiActive()) {
                System.out.println("AI engine is deactivated for session ID: " + session.getId());
                return;
            }

            // Retrieve last 10 messages for conversation context
            org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
            List<ChatHistory> latestHistory = chatHistoryRepository.findLatestMessages(session.getId(), garage.getGarageId(), pageable);

            List<Message> aiMessages = new ArrayList<>();

            // Set liability warning prompt as required
            String systemPrompt = String.format(
                "You are a garage assistant. You may diagnose symptoms, but you must NEVER guarantee a specific repair " +
                "or quote a final price based on text. Always suggest an in-person diagnostic. " +
                "You assist customers for '%s'. Ensure you use tools when booking or checking slots. " +
                "The customer's name is %s, phone is %s. The current garage ID is %d.",
                garage.getName(), session.getCustomerName(), session.getCustomerPhoneNumber(), garage.getGarageId()
            );

            aiMessages.add(new SystemMessage(systemPrompt));

            // Load context chronologically
            for (int i = latestHistory.size() - 1; i >= 0; i--) {
                ChatHistory hist = latestHistory.get(i);
                if ("CUSTOMER".equals(hist.getSender())) {
                    aiMessages.add(new UserMessage(hist.getMessage()));
                } else if ("AI".equals(hist.getSender())) {
                    aiMessages.add(new AssistantMessage(hist.getMessage()));
                }
            }

            String aiResponse;
            try {
                aiResponse = chatClient.prompt(new Prompt(aiMessages))
                    .call()
                    .content();
            } catch (Exception e) {
                System.err.println("Gemini AI API Call failed: " + e.getMessage());
                if (e.getMessage() != null && (e.getMessage().contains("quota") || e.getMessage().contains("429"))) {
                    aiResponse = "I'm experiencing high traffic right now. Please try again in 30 seconds, or reply to reach our team.";
                } else {
                    aiResponse = "I'm sorry, I'm having trouble connecting right now. Please try again in a moment.";
                }
            }

            if (aiResponse == null || aiResponse.trim().isEmpty()) {
                aiResponse = "I'm sorry, I couldn't process that. Please contact support.";
            }

            // Save AI reply log
            ChatHistory aiMsg = ChatHistory.builder()
                .chatSession(session)
                .sender("AI")
                .message(aiResponse)
                .timestamp(LocalDateTime.now())
                .garage(garage)
                .build();
            chatHistoryRepository.save(aiMsg);

            whatsAppNotificationService.sendTextMessage(customerPhone, aiResponse);

        } catch (Exception e) {
            System.err.println("Fatal error in WhatsApp AI thread: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
