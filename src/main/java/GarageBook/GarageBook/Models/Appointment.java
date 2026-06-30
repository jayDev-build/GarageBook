package GarageBook.GarageBook.Models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "appointment")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "appointmentIdGenrator")
    @SequenceGenerator(name = "appointmentIdGenrator", sequenceName = "appointment_id_seq", allocationSize = 20, initialValue = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    private LocalDateTime appointmentTime;

    private String status; // PENDING, CONFIRMED, CANCELLED

    @Column(columnDefinition = "TEXT")
    private String symptoms;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "garage_id", nullable = false)
    private Garage garage;
}
