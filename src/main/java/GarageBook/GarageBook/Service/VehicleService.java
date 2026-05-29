package GarageBook.GarageBook.Service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import GarageBook.GarageBook.Dto.Request.VehicleRequestDto;
import GarageBook.GarageBook.Dto.Response.VehicleResponseDto;
import GarageBook.GarageBook.Models.Owner;
import GarageBook.GarageBook.Models.Vehicle;
import GarageBook.GarageBook.Repository.OwnerRepository;
import GarageBook.GarageBook.Repository.VehicleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class VehicleService {
    private final VehicleRepository vehicleRepository;
    private final OwnerRepository ownerRepository;

    public VehicleService(VehicleRepository vehicleRepository, OwnerRepository ownerRepository) {
        this.vehicleRepository = vehicleRepository;
        this.ownerRepository = ownerRepository;
    }

    public VehicleResponseDto createVehicle(VehicleRequestDto request) {
        Owner owner = ownerRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner not found with id: " + request.getOwnerId()));

        Vehicle vehicle = Vehicle.builder()
                .owner(owner)
                .vehicleType(request.getVehicleType())
                .vehicleNumber(request.getVehicleNumber())
                .build();

        Vehicle saved = vehicleRepository.save(vehicle);
        return mapToResponse(saved);
    }

    public List<VehicleResponseDto> getAllVehicles() {
        return vehicleRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public VehicleResponseDto getVehicleById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found with id: " + id));
        return mapToResponse(vehicle);
    }

    public VehicleResponseDto updateVehicle(Long id, VehicleRequestDto request) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found with id: " + id));

        Owner owner = ownerRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner not found with id: " + request.getOwnerId()));

        vehicle.setOwner(owner);
        vehicle.setVehicleType(request.getVehicleType());
        vehicle.setVehicleNumber(request.getVehicleNumber());

        Vehicle updated = vehicleRepository.save(vehicle);
        return mapToResponse(updated);
    }

    public void deleteVehicle(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found with id: " + id));
        vehicleRepository.delete(vehicle);
    }

    private VehicleResponseDto mapToResponse(Vehicle vehicle) {
        return VehicleResponseDto.builder()
                .id(vehicle.getId())
                .ownerId(vehicle.getOwner().getId())
                .ownerName(vehicle.getOwner().getName())
                .vehicleType(vehicle.getVehicleType())
                .vehicleNumber(vehicle.getVehicleNumber())
                .build();
    }
}
