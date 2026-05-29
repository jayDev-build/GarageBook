package GarageBook.GarageBook.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import GarageBook.GarageBook.Models.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserName(String userName);
}
