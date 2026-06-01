package GarageBook.GarageBook.Dto.Request;

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
public class UpdateServicePartRequestDto {
    private Long partId;
    private Integer quantity;
    private Long pricePerUnit;
}
