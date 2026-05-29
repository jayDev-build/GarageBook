package GarageBook.GarageBook.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import GarageBook.GarageBook.Models.ServicePart;

@Repository
public interface ServicePartRepository extends JpaRepository<ServicePart, Long> {

}