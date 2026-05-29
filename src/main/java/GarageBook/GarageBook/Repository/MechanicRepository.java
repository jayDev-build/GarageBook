package GarageBook.GarageBook.Repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import GarageBook.GarageBook.Models.Garage;
import GarageBook.GarageBook.Models.Mechanic;

@Repository
public interface MechanicRepository extends JpaRepository<Mechanic, Long> {
    Optional<Mechanic> findByPhoneNumber(String phoneNumber);

    Optional<Mechanic> findByAdhaarNumber(String adhaarNumber);

    List<Mechanic> findByGarage(Garage garage);

}
