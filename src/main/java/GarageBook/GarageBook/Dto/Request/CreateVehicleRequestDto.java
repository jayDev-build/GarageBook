package GarageBook.GarageBook.Dto.Request;

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
public class CreateVehicleRequestDto {
    private Long ownerId;
    private VehicleType vehicleType;
    private String vehicleNumber;
}
