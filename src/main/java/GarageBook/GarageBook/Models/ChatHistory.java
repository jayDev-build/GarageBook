package GarageBook.GarageBook.Models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "chat_history")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chatHistoryIdGenrator")
    @SequenceGenerator(name = "chatHistoryIdGenrator", sequenceName = "chat_history_id_seq", allocationSize = 20, initialValue = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_session_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private ChatSession chatSession;

    private String sender; // CUSTOMER, AI, AGENT

    @Column(columnDefinition = "TEXT")
    private String message;

    private LocalDateTime timestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "garage_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Garage garage;
}
