package GarageBook.GarageBook.Service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import GarageBook.GarageBook.Dto.Request.GarageRequestDto;
import GarageBook.GarageBook.Dto.Response.GarageResponseDto;
import GarageBook.GarageBook.Models.Garage;
import GarageBook.GarageBook.Repository.GarageRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import GarageBook.GarageBook.Models.User;

@Service
public class GarageService {
    private final GarageRepository garageRepository;

    public GarageService(GarageRepository garageRepository) {
        this.garageRepository = garageRepository;
    }

    private Garage getAuthenticatedUserGarage() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User currentUser = (User) authentication.getPrincipal();
            Garage garage = currentUser.getGarage();
            if (garage == null) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not associated with any garage");
            }
            return garage;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated");
    }

    public GarageResponseDto createGarage(GarageRequestDto request) {
        Garage garage = Garage.builder()
                .name(request.getName())
                .address(request.getAddress())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .GSTNumber(request.getGSTNumber())
                .build();

        Garage saved = garageRepository.save(garage);
        return mapToResponse(saved);
    }

    public List<GarageResponseDto> getAllGarages() {
        Garage garage = getAuthenticatedUserGarage();
        return List.of(mapToResponse(garage));
    }

    public GarageResponseDto getGarageById(Long id) {
        Garage garage = getAuthenticatedUserGarage();
        if (!garage.getGarageId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to garage: " + id);
        }
        return mapToResponse(garage);
    }

    public GarageResponseDto updateGarage(Long id, GarageRequestDto request) {
        Garage garage = getAuthenticatedUserGarage();
        if (!garage.getGarageId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to garage: " + id);
        }

        garage.setName(request.getName());
        garage.setAddress(request.getAddress());
        garage.setPhoneNumber(request.getPhoneNumber());
        garage.setEmail(request.getEmail());
        garage.setGSTNumber(request.getGSTNumber());

        Garage updated = garageRepository.save(garage);
        return mapToResponse(updated);
    }

    public void deleteGarage(Long id) {
        Garage garage = getAuthenticatedUserGarage();
        if (!garage.getGarageId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to garage: " + id);
        }
        garageRepository.delete(garage);
    }

    private GarageResponseDto mapToResponse(Garage garage) {
        return GarageResponseDto.builder()
                .garageId(garage.getGarageId())
                .name(garage.getName())
                .address(garage.getAddress())
                .phoneNumber(garage.getPhoneNumber())
                .email(garage.getEmail())
                .GSTNumber(garage.getGSTNumber())
                .build();
    }
}
