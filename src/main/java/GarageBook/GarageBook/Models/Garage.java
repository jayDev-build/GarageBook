package GarageBook.GarageBook.Models;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Garage")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Garage {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "garage_sequence")
    @SequenceGenerator(name = "garage_sequence", sequenceName = "garage_sequence", allocationSize = 1)
    private Long garageId;

    @Column(unique = true)
    private String userName;

    private String password;

    private String name;
    private String address;
    private String phoneNumber;
    private String email;
    private String GSTNumber;

    @OneToMany(mappedBy = "garage", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    List<ServiceBooking> services = new ArrayList<>();

    @OneToMany(mappedBy = "garage", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    List<Mechanic> mechanics = new ArrayList<>();
}
