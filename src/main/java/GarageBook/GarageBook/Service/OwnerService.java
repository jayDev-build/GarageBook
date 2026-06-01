package GarageBook.GarageBook.Service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import GarageBook.GarageBook.Dto.Request.CreateOwnerRequestDto;
import GarageBook.GarageBook.Dto.Request.UpdateOwnerRequestDto;
import GarageBook.GarageBook.Dto.Response.OwnerResponseDto;
import GarageBook.GarageBook.Models.Owner;
import GarageBook.GarageBook.Models.Garage;
import GarageBook.GarageBook.Models.User;
import GarageBook.GarageBook.Repository.OwnerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
public class OwnerService {
    private final OwnerRepository ownerRepository;

    public OwnerService(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
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

    private void checkOwnerAccess(Owner owner, Garage garage) {
        boolean hasVehicleInGarage = owner.getVehicles() != null && owner.getVehicles().stream()
                .anyMatch(v -> v.getGarage() != null && v.getGarage().getGarageId().equals(garage.getGarageId()));
        if (!hasVehicleInGarage) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to owner: " + owner.getId());
        }
    }

    public OwnerResponseDto createOwner(CreateOwnerRequestDto request) {
        Owner owner = Owner.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .build();

        Owner saved = ownerRepository.save(owner);
        return mapToResponse(saved);
    }

    public List<OwnerResponseDto> getAllOwners() {
        Garage garage = getAuthenticatedUserGarage();
        return ownerRepository.findByGarage(garage).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public OwnerResponseDto getOwnerById(Long id) {
        Garage garage = getAuthenticatedUserGarage();
        Owner owner = ownerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner not found with id: " + id));
        checkOwnerAccess(owner, garage);
        return mapToResponse(owner);
    }

    public OwnerResponseDto updateOwner(Long id, UpdateOwnerRequestDto request) {
        Garage garage = getAuthenticatedUserGarage();
        Owner owner = ownerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner not found with id: " + id));
        checkOwnerAccess(owner, garage);

        owner.setName(request.getName());
        owner.setEmail(request.getEmail());
        owner.setPhoneNumber(request.getPhoneNumber());

        Owner updated = ownerRepository.save(owner);
        return mapToResponse(updated);
    }

    public void deleteOwner(Long id) {
        Garage garage = getAuthenticatedUserGarage();
        Owner owner = ownerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner not found with id: " + id));
        checkOwnerAccess(owner, garage);
        ownerRepository.delete(owner);
    }


    private OwnerResponseDto mapToResponse(Owner owner) {
        return OwnerResponseDto.builder()
                .id(owner.getId())
                .name(owner.getName())
                .email(owner.getEmail())
                .phoneNumber(owner.getPhoneNumber())
                .build();
    }
}
