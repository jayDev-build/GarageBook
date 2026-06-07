package GarageBook.GarageBook.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import GarageBook.GarageBook.Models.Owner;
import GarageBook.GarageBook.Models.Garage;

import java.util.List;
import java.util.Optional;

@Repository
public interface OwnerRepository extends JpaRepository<Owner, Long> {
    Optional<Owner> findByEmail(String email);

    Optional<Owner> findByPhoneNumber(String phoneNumber);

    @Query("SELECT DISTINCT o FROM Owner o LEFT JOIN o.vehicles v WHERE o.garage = :garage OR v.garage = :garage")
    List<Owner> findByGarage(@Param("garage") Garage garage);
}
