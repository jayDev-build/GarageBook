package GarageBook.GarageBook.Dto.Response;

import java.time.LocalDateTime;
import java.util.List;
import GarageBook.GarageBook.Enums.BookingStatus;
import GarageBook.GarageBook.Enums.ServiceType;
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
public class ServiceBookingResponseDto {
    private Long id;
    private Long vehicleId;
    private String vehicleNumber;
    private ServiceType serviceType;
    private LocalDateTime bookingTime;
    private BookingStatus bookingStatus;
    private Long totalAmount;
    private Long labourCharges;
    private Long garageId;
    private String garageName;
    private String description;
    private List<ServicePartResponseDto> serviceParts;
}
