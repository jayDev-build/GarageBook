package GarageBook.GarageBook.Dto.Request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class RegisterUserDto {
    private String username;
    private String password;

}