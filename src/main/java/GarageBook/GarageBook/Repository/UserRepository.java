package GarageBook.GarageBook.Repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import GarageBook.GarageBook.Models.User;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.id = :userId AND u.garage.garageId = :garageId")
    Optional<User> findByUserIdAndGarageId(@Param("userId") Integer userId, @Param("garageId") Long garageId);
}