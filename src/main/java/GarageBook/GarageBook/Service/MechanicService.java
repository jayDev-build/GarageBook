package GarageBook.GarageBook.Service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import GarageBook.GarageBook.Dto.Request.MechanicRequestDto;
import GarageBook.GarageBook.Dto.Response.MechanicResponseDto;
import GarageBook.GarageBook.Models.Garage;
import GarageBook.GarageBook.Models.Mechanic;
import GarageBook.GarageBook.Repository.GarageRepository;
import GarageBook.GarageBook.Repository.MechanicRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import GarageBook.GarageBook.Models.User;

@Service
public class MechanicService {
    private final MechanicRepository mechanicRepository;
    private final GarageRepository garageRepository;

    public MechanicService(MechanicRepository mechanicRepository, GarageRepository garageRepository) {
        this.mechanicRepository = mechanicRepository;
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

    public MechanicResponseDto createMechanic(MechanicRequestDto request) {
        Garage garage = getAuthenticatedUserGarage();

        Mechanic mechanic = Mechanic.builder()
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .adhaarNumber(request.getAdhaarNumber())
                .address(request.getAddress())
                .garage(garage)
                .build();

        Mechanic saved = mechanicRepository.save(mechanic);
        return mapToResponse(saved);
    }

    public List<MechanicResponseDto> getAllMechanics() {
        Garage garage = getAuthenticatedUserGarage();
        return mechanicRepository.findByGarage(garage).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public MechanicResponseDto getMechanicById(Long id) {
        Garage garage = getAuthenticatedUserGarage();
        Mechanic mechanic = mechanicRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mechanic not found with id: " + id));

        if (mechanic.getGarage() == null || !mechanic.getGarage().getGarageId().equals(garage.getGarageId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to mechanic: " + id);
        }

        return mapToResponse(mechanic);
    }

    public MechanicResponseDto updateMechanic(Long id, MechanicRequestDto request) {
        Garage garage = getAuthenticatedUserGarage();
        Mechanic mechanic = mechanicRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mechanic not found with id: " + id));

        if (mechanic.getGarage() == null || !mechanic.getGarage().getGarageId().equals(garage.getGarageId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to mechanic: " + id);
        }

        mechanic.setName(request.getName());
        mechanic.setPhoneNumber(request.getPhoneNumber());
        mechanic.setAdhaarNumber(request.getAdhaarNumber());
        mechanic.setAddress(request.getAddress());
        mechanic.setGarage(garage);

        Mechanic updated = mechanicRepository.save(mechanic);
        return mapToResponse(updated);
    }

    public void deleteMechanic(Long id) {
        Garage garage = getAuthenticatedUserGarage();
        Mechanic mechanic = mechanicRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mechanic not found with id: " + id));

        if (mechanic.getGarage() == null || !mechanic.getGarage().getGarageId().equals(garage.getGarageId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to mechanic: " + id);
        }

        mechanicRepository.delete(mechanic);
    }

    private MechanicResponseDto mapToResponse(Mechanic mechanic) {
        return MechanicResponseDto.builder()
                .mechanicId(mechanic.getMechanicId())
                .name(mechanic.getName())
                .phoneNumber(mechanic.getPhoneNumber())
                .adhaarNumber(mechanic.getAdhaarNumber())
                .address(mechanic.getAddress())
                .garageId(mechanic.getGarage() != null ? mechanic.getGarage().getGarageId() : null)
                .garageName(mechanic.getGarage() != null ? mechanic.getGarage().getName() : null)
                .build();
    }
}
