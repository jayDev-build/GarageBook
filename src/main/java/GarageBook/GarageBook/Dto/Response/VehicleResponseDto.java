package GarageBook.GarageBook.Dto.Response;

import GarageBook.GarageBook.Enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VehicleResponseDto {
    private Long id;
    private Long ownerId;
    private String ownerName;
    private VehicleType vehicleType;
    private String vehicleNumber;
}
