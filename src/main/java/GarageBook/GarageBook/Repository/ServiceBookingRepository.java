package GarageBook.GarageBook.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import GarageBook.GarageBook.Models.ServiceBooking;

import java.util.List;
import GarageBook.GarageBook.Models.Garage;

@Repository
public interface ServiceBookingRepository extends JpaRepository<ServiceBooking, Long> {
    List<ServiceBooking> findByGarage(Garage garage);
}
