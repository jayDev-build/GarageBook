package GarageBook.GarageBook.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import GarageBook.GarageBook.Models.Garage;
import java.util.List;

import GarageBook.GarageBook.Models.Vehicle;

import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    List<Vehicle> findByGarage(Garage garage);
    Optional<Vehicle> findByVehicleNumber(String vehicleNumber);
}
