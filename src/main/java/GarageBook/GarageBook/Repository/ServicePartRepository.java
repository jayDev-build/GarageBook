package GarageBook.GarageBook.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import GarageBook.GarageBook.Models.ServiceBooking;
import GarageBook.GarageBook.Models.ServicePart;

@Repository
public interface ServicePartRepository extends JpaRepository<ServicePart, Long> {
    @Modifying
    @Query("DELETE FROM ServicePart sp WHERE sp.serviceBooking = :booking")
    void deleteByServiceBooking(ServiceBooking booking);
}