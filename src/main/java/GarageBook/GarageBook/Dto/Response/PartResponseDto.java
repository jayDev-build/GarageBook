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
public class PartResponseDto {
    private Long partId;
    private String partName;
    private String partSize;
    private String partNumber;
    private Integer stockQuantity;
    private Long defaultPrice;
}
