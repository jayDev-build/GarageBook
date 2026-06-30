package GarageBook.GarageBook.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import GarageBook.GarageBook.WhatsApp.GarageAiService;

@RestController
@RequestMapping("/api/webhook/whatsapp")
public class WhatsAppWebhookController {

    private final GarageAiService garageAiService;

    public WhatsAppWebhookController(GarageAiService garageAiService) {
        this.garageAiService = garageAiService;
    }

    @GetMapping
    public ResponseEntity<String> verifyWebhook(
            @RequestParam(name = "hub.mode", required = false) String mode,
            @RequestParam(name = "hub.verify_token", required = false) String verifyToken,
            @RequestParam(name = "hub.challenge", required = false) String challenge) {

        // Default verification token
        String expectedToken = "garage_book_verify_token";

        if ("subscribe".equals(mode) && expectedToken.equals(verifyToken)) {
            System.out.println("Meta Webhook verified successfully!");
            return ResponseEntity.ok(challenge);
        }

        System.err.println("Webhook verification failed!");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @PostMapping
    public ResponseEntity<Void> receiveMessage(@RequestBody String payload) {
        // Safe, instant HTTP 200 return to prevent timeouts
        garageAiService.processIncomingWebhookAsync(payload);
        return ResponseEntity.ok().build();
    }
}
