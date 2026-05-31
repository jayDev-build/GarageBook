package GarageBook.GarageBook.Authentication;

import GarageBook.GarageBook.Dto.Request.RegisterUserDto;
import GarageBook.GarageBook.Dto.Response.LoginResponse;
import GarageBook.GarageBook.Dto.Response.LoginUserDto;
import GarageBook.GarageBook.Models.User;
import GarageBook.GarageBook.WebSecurity.JwtService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RequestMapping("/auth")
@RestController
public class AuthenticationController {
    private final JwtService jwtService;

    private final AuthenticationService authenticationService;

    public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/signup")
    public ResponseEntity<User> register(@RequestBody RegisterUserDto registerUserDto) {
        User registeredUser = authenticationService.signup(registerUserDto);

        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginUserDto loginUserDto,
            HttpServletResponse response) {
        User authenticatedUser = authenticationService.authenticate(loginUserDto);

        String jwtToken = jwtService.generateToken(authenticatedUser);

        try {
            String cookieValue = "Bearer " + jwtToken;
            ResponseCookie cookie = ResponseCookie.from("jwt", URLEncoder.encode(cookieValue, StandardCharsets.UTF_8))
                    .httpOnly(true)
                    .secure(false) // Set to true in production over HTTPS
                    .path("/")
                    .maxAge(jwtService.getExpirationTime() / 1000)
                    .sameSite("None")
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        } catch (Exception e) {
            // Log or handle encoding issue safely
        }

        LoginResponse loginResponse = LoginResponse
                .builder()
                .token(jwtToken)
                .expiresIn(jwtService.getExpirationTime())
                .build();

        return ResponseEntity.ok(loginResponse);
    }
}