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
public class GarageRequestDto {
    private String name;
    private String address;
    private String phoneNumber;
    private String email;
    private String GSTNumber;
}
