package GarageBook.GarageBook.Models;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "service_parts",
    uniqueConstraints = @UniqueConstraint(columnNames = {"part_id", "service_id"})
)
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceParts {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "servicePartsIdGenrator")
    @SequenceGenerator(name = "servicePartsIdGenrator", sequenceName = "service_parts_id_seq", allocationSize = 20, initialValue = 1)
    private Long id;
   
    @ManyToOne
    @JoinColumn(name = "part_id")
    private Part part;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private ServiceBooking serviceBooking;

    private Integer quantity;
    
    private Long pricePerUnit;

    private Long totalPrice;

}
