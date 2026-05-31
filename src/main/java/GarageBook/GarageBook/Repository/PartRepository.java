package GarageBook.GarageBook.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import GarageBook.GarageBook.Models.Garage;
import java.util.List;

import GarageBook.GarageBook.Models.Part;

@Repository
public interface PartRepository extends JpaRepository<Part, Long> {
    List<Part> findByGarage(Garage garage);
}
