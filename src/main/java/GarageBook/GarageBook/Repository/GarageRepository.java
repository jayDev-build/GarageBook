package GarageBook.GarageBook.Repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import GarageBook.GarageBook.Models.Garage;

@Repository
public interface GarageRepository extends JpaRepository<Garage, Long> {
}
