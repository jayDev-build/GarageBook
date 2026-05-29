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
public class PartRequestDto {
    private String partName;
    private String partSize;
    private String partNumber;
    private Integer stockQuantity;
    private Long defaultPrice;
}
