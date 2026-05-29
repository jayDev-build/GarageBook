package GarageBook.GarageBook.Models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import GarageBook.GarageBook.Enums.BookingStatus;
import GarageBook.GarageBook.Enums.ServiceType;

import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "service_booking")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceBooking {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "serviceBookingIdGenrator")
    @SequenceGenerator(name = "serviceBookingIdGenrator", sequenceName = "service_booking_id_seq", allocationSize = 20, initialValue = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ServiceType serviceType;

    private LocalDateTime bookingTime;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private BookingStatus bookingStatus;

    private Long totalAmount;

    @ManyToOne
    @JoinColumn(name = "garage_id")
    private Garage garage;

    @OneToMany(mappedBy = "serviceBooking", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ServicePart> serviceParts = new ArrayList<>();

    public void addServicePart(ServicePart serviceParts) {
        this.serviceParts.add(serviceParts);
        serviceParts.setServiceBooking(this);
    }

    public void removeServicePart(ServicePart serviceParts) {
        this.serviceParts.remove(serviceParts);
        serviceParts.setServiceBooking(null);
    }
}
