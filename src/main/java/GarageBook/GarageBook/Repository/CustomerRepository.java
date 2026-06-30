package GarageBook.GarageBook.Repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import GarageBook.GarageBook.Models.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByPhoneNumberAndGarageGarageId(String phoneNumber, Long garageId);
}
