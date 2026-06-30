package GarageBook.GarageBook.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "customer")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "customerIdGenrator")
    @SequenceGenerator(name = "customerIdGenrator", sequenceName = "customer_id_seq", allocationSize = 20, initialValue = 1)
    private Long id;

    private String name;

    @Column(unique = true)
    private String phoneNumber;

    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "garage_id", nullable = false)
    private Garage garage;
}
