package GarageBook.GarageBook.Models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "chat_session")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chatSessionIdGenrator")
    @SequenceGenerator(name = "chatSessionIdGenrator", sequenceName = "chat_session_id_seq", allocationSize = 20, initialValue = 1)
    private Long id;

    private String customerPhoneNumber;

    private String customerName;

    @Builder.Default
    private Boolean isAiActive = true;

    private LocalDateTime lastMessageTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "garage_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Garage garage;

    @OneToMany(mappedBy = "chatSession", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<ChatHistory> chatHistories = new ArrayList<>();
}
