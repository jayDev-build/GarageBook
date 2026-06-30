package GarageBook.GarageBook.Repository;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import GarageBook.GarageBook.Models.ChatHistory;

@Repository
public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {
    List<ChatHistory> findByChatSessionIdAndGarageGarageIdOrderByTimestampAsc(Long sessionId, Long garageId);

    @Query("SELECT h FROM ChatHistory h WHERE h.chatSession.id = :sessionId AND h.garage.garageId = :garageId ORDER BY h.timestamp DESC")
    List<ChatHistory> findLatestMessages(@Param("sessionId") Long sessionId, @Param("garageId") Long garageId, Pageable pageable);
}
