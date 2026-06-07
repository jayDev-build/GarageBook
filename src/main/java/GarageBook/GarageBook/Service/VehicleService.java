package GarageBook.GarageBook.Service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import GarageBook.GarageBook.Dto.Request.CreateVehicleRequestDto;
import GarageBook.GarageBook.Dto.Request.UpdateVehicleRequestDto;
import GarageBook.GarageBook.Dto.Response.VehicleResponseDto;
import GarageBook.GarageBook.Models.Owner;
import GarageBook.GarageBook.Models.Vehicle;
import GarageBook.GarageBook.Models.Garage;
import GarageBook.GarageBook.Models.User;
import GarageBook.GarageBook.Repository.OwnerRepository;
import GarageBook.GarageBook.Repository.VehicleRepository;
import GarageBook.GarageBook.Repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
public class VehicleService {
    private final VehicleRepository vehicleRepository;
    private final OwnerRepository ownerRepository;
    private final UserRepository userRepository;

    public VehicleService(VehicleRepository vehicleRepository, OwnerRepository ownerRepository, UserRepository userRepository) {
        this.vehicleRepository = vehicleRepository;
        this.ownerRepository = ownerRepository;
        this.userRepository = userRepository;
    }

    public VehicleResponseDto createVehicle(CreateVehicleRequestDto request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Garage garage = currentUser.getGarage();
        if (garage == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not associated with any garage");
        }

        Owner owner = ownerRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner not found with id: " + request.getOwnerId()));

        Vehicle vehicle = Vehicle.builder()
                .owner(owner)
                .vehicleType(request.getVehicleType())
                .vehicleNumber(request.getVehicleNumber())
                .garage(garage)
                .build();

        Vehicle saved = vehicleRepository.save(vehicle);
        return mapToResponse(saved);
    }

    public List<VehicleResponseDto> getAllVehicles() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Garage garage = currentUser.getGarage();
        if (garage == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not associated with any garage");
        }
        return vehicleRepository.findByGarage(garage).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public VehicleResponseDto getVehicleById(Long id) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found with id: " + id));

        userRepository.findByUserIdAndGarageId(currentUser.getId(), vehicle.getGarage().getGarageId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to vehicle: " + id));

        return mapToResponse(vehicle);
    }

    public VehicleResponseDto updateVehicle(Long id, UpdateVehicleRequestDto request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found with id: " + id));

        userRepository.findByUserIdAndGarageId(currentUser.getId(), vehicle.getGarage().getGarageId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to vehicle: " + id));

        vehicle.setVehicleType(request.getVehicleType());
        vehicle.setVehicleNumber(request.getVehicleNumber());

        Vehicle updated = vehicleRepository.save(vehicle);
        return mapToResponse(updated);
    }

    public void deleteVehicle(Long id) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found with id: " + id));

        userRepository.findByUserIdAndGarageId(currentUser.getId(), vehicle.getGarage().getGarageId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to vehicle: " + id));

        vehicleRepository.delete(vehicle);
    }

    private VehicleResponseDto mapToResponse(Vehicle vehicle) {
        return VehicleResponseDto.builder()
                .id(vehicle.getId())
                .ownerId(vehicle.getOwner().getId())
                .ownerName(vehicle.getOwner().getName())
                .vehicleType(vehicle.getVehicleType())
                .vehicleNumber(vehicle.getVehicleNumber())
                .garageId(vehicle.getGarage() != null ? vehicle.getGarage().getGarageId() : null)
                .garageName(vehicle.getGarage() != null ? vehicle.getGarage().getName() : null)
                .build();
    }
}
