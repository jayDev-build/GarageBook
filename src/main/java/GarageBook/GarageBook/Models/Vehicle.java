package GarageBook.GarageBook.Models;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

import GarageBook.GarageBook.Enums.VehicleType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vehicle")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "vehicleIdGenrator")
    @SequenceGenerator(name = "vehicleIdGenrator", sequenceName = "vehicle_id_seq", allocationSize = 20, initialValue = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private Owner owner;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private VehicleType vehicleType;

    @Column(unique = true)
    private String vehicleNumber;

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ServiceBooking> bookings = new ArrayList<>();

}
