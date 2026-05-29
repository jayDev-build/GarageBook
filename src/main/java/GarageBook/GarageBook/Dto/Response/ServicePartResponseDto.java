package GarageBook.GarageBook.Dto.Response;

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
public class ServicePartResponseDto {
    private Long id;
    private Long partId;
    private String partName;
    private Long serviceBookingId;
    private Integer quantity;
    private Long pricePerUnit;
    private Long totalPrice;
}
