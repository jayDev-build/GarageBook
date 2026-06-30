package GarageBook.GarageBook.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import GarageBook.GarageBook.Models.ChatSession;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    List<ChatSession> findByGarageGarageId(Long garageId);
    Optional<ChatSession> findByCustomerPhoneNumberAndGarageGarageId(String phoneNumber, Long garageId);
    Optional<ChatSession> findByIdAndGarageGarageId(Long id, Long garageId);
}
