package GarageBook.GarageBook.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import GarageBook.GarageBook.Dto.Response.UserResponseDto;
import GarageBook.GarageBook.Models.User;
import GarageBook.GarageBook.Repository.UserRepository;

@Service    
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserResponseDto> allUsers() {
        List<User> users = new ArrayList<>();
        userRepository.findAll().forEach(users::add);
        return users.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public UserResponseDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        return mapToResponse(user);
    }

    public UserResponseDto mapToResponse(User user) {
        if (user == null) {
            return null;
        }
        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .garageId(user.getGarage() != null ? user.getGarage().getGarageId() : null)
                .garageName(user.getGarage() != null ? user.getGarage().getName() : null)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}

