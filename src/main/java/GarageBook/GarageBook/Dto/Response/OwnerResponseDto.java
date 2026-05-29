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
public class OwnerResponseDto {
    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
}
