package GarageBook.GarageBook.Dto.Response;

import java.time.LocalDateTime;
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
public class UserResponseDto {
    private Integer id;
    private String username;
    private Long garageId;
    private String garageName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
