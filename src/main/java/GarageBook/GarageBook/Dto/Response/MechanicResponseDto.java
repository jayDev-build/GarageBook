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
public class MechanicResponseDto {
    private Long mechanicId;
    private String name;
    private String phoneNumber;
    private String adhaarNumber;
    private String address;
    private Long garageId;
    private String garageName;
}
