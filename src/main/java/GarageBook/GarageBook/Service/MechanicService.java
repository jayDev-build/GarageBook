package GarageBook.GarageBook.Service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import GarageBook.GarageBook.Dto.Request.CreateMechanicRequestDto;
import GarageBook.GarageBook.Dto.Request.UpdateMechanicRequestDto;
import GarageBook.GarageBook.Dto.Response.MechanicResponseDto;
import GarageBook.GarageBook.Models.Garage;
import GarageBook.GarageBook.Models.Mechanic;
import GarageBook.GarageBook.Repository.MechanicRepository;
import GarageBook.GarageBook.Repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.context.SecurityContextHolder;
import GarageBook.GarageBook.Models.User;

@Service
public class MechanicService {
    private final MechanicRepository mechanicRepository;
    private final UserRepository userRepository;

    public MechanicService(MechanicRepository mechanicRepository, UserRepository userRepository) {
        this.mechanicRepository = mechanicRepository;
        this.userRepository = userRepository;
    }

    public MechanicResponseDto createMechanic(CreateMechanicRequestDto request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Garage garage = currentUser.getGarage();
        if (garage == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not associated with any garage");
        }

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
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Garage garage = currentUser.getGarage();
        if (garage == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not associated with any garage");
        }
        return mechanicRepository.findByGarage(garage).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public MechanicResponseDto getMechanicById(Long id) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Mechanic mechanic = mechanicRepository.findById(id)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mechanic not found with id: " + id));

        userRepository.findByUserIdAndGarageId(currentUser.getId(), mechanic.getGarage().getGarageId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to mechanic: " + id));

        return mapToResponse(mechanic);
    }

    public MechanicResponseDto updateMechanic(Long id, UpdateMechanicRequestDto request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Mechanic mechanic = mechanicRepository.findById(id)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mechanic not found with id: " + id));

        userRepository.findByUserIdAndGarageId(currentUser.getId(), mechanic.getGarage().getGarageId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to mechanic: " + id));

        mechanic.setPhoneNumber(request.getPhoneNumber());
        mechanic.setAddress(request.getAddress());

        Mechanic updated = mechanicRepository.save(mechanic);
        return mapToResponse(updated);
    }

    public void deleteMechanic(Long id) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Mechanic mechanic = mechanicRepository.findById(id)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mechanic not found with id: " + id));

        userRepository.findByUserIdAndGarageId(currentUser.getId(), mechanic.getGarage().getGarageId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to mechanic: " + id));

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
