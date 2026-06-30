package GarageBook.GarageBook.Controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import GarageBook.GarageBook.Models.ChatHistory;
import GarageBook.GarageBook.Models.ChatSession;
import GarageBook.GarageBook.Models.Garage;
import GarageBook.GarageBook.Models.User;
import GarageBook.GarageBook.Repository.ChatHistoryRepository;
import GarageBook.GarageBook.Repository.ChatSessionRepository;

@RestController
@RequestMapping("/api/whatsapp/sessions")
public class ChatSessionController {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatHistoryRepository chatHistoryRepository;

    public ChatSessionController(
            ChatSessionRepository chatSessionRepository,
            ChatHistoryRepository chatHistoryRepository) {
        this.chatSessionRepository = chatSessionRepository;
        this.chatHistoryRepository = chatHistoryRepository;
    }

    private Garage getAuthenticatedUserGarage() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Garage garage = currentUser.getGarage();
        if (garage == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not associated with any garage");
        }
        return garage;
    }

    @GetMapping
    public ResponseEntity<List<ChatSession>> getSessions() {
        Garage garage = getAuthenticatedUserGarage();
        List<ChatSession> sessions = chatSessionRepository.findByGarageGarageId(garage.getGarageId());
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<ChatHistory>> getSessionHistory(@PathVariable("id") Long id) {
        Garage garage = getAuthenticatedUserGarage();
        
        // Ensure chat session belongs to the user's garage (Data Isolation)
        ChatSession session = chatSessionRepository.findByIdAndGarageGarageId(id, garage.getGarageId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat session not found for this garage"));

        List<ChatHistory> history = chatHistoryRepository.findByChatSessionIdAndGarageGarageIdOrderByTimestampAsc(id, garage.getGarageId());
        return ResponseEntity.ok(history);
    }

    @PatchMapping("/{id}/toggle-ai")
    public ResponseEntity<ChatSession> toggleAi(
            @PathVariable("id") Long id,
            @RequestParam("isAiActive") boolean isAiActive) {
        Garage garage = getAuthenticatedUserGarage();

        // Ensure session belongs to user's garage (Data Isolation)
        ChatSession session = chatSessionRepository.findByIdAndGarageGarageId(id, garage.getGarageId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat session not found for this garage"));

        session.setIsAiActive(isAiActive);
        ChatSession updated = chatSessionRepository.save(session);
        return ResponseEntity.ok(updated);
    }
}
