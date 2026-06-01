package GarageBook.GarageBook.Controller;

import GarageBook.GarageBook.Dto.Response.UserResponseDto;
import GarageBook.GarageBook.Models.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import GarageBook.GarageBook.Service.UserService;
import java.util.List;
import org.springframework.http.ResponseEntity;

@RequestMapping("/users")
@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> authenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        UserResponseDto freshUser = userService.getUserByUsername(currentUser.getUsername());
        return ResponseEntity.ok(freshUser);
    }

    @GetMapping("/")
    public ResponseEntity<List<UserResponseDto>> allUsers() {
        List<UserResponseDto> users = userService.allUsers();
        return ResponseEntity.ok(users);
    }
}

