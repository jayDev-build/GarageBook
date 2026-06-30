package GarageBook.GarageBook.Repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import GarageBook.GarageBook.Models.Appointment;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByGarageGarageId(Long garageId);
    List<Appointment> findByGarageGarageIdAndAppointmentTimeBetween(Long garageId, LocalDateTime start, LocalDateTime end);
}
