package GarageBook.GarageBook.Models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mechanic")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mechanic {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mechanicSequenceGenerator")
    @SequenceGenerator(name = "mechanicSequenceGenerator", sequenceName = "mechanic_sequence", allocationSize = 1)
    private Long mechanicId;

    private String name;
    @Column(unique = true)
    private String phoneNumber;
    @Column(unique = true)
    private String adhaarNumber;
    private String address;

    @ManyToOne
    @JoinColumn(name = "garage_id")
    private Garage garage;

}
