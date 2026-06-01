package GarageBook.GarageBook.Dto.Request;

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
public class UpdateServiceBookingRequestDto {
    private ServiceType serviceType;
    private LocalDateTime bookingTime;
    private BookingStatus bookingStatus;
    private Long totalAmount;
    private Long labourCharges;
    private List<CreateServicePartRequestDto> serviceParts;
}
