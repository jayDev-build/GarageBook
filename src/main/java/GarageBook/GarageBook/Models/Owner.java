package GarageBook.GarageBook.Models;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "owner")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Owner{
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ownerIdGenrator")
    @SequenceGenerator(name = "ownerIdGenrator", sequenceName = "owner_id_seq", allocationSize = 20, initialValue = 1)
    private Long id;

    private String name;
    
    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String phoneNumber;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Vehicle> vehicles = new ArrayList<>();
}