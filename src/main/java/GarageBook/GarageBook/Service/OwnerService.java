package GarageBook.GarageBook.Service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import GarageBook.GarageBook.Dto.Request.OwnerRequestDto;
import GarageBook.GarageBook.Dto.Response.OwnerResponseDto;
import GarageBook.GarageBook.Models.Owner;
import GarageBook.GarageBook.Repository.OwnerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OwnerService {
    private final OwnerRepository ownerRepository;

    public OwnerService(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    public OwnerResponseDto createOwner(OwnerRequestDto request) {
        Owner owner = Owner.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .build();

        Owner saved = ownerRepository.save(owner);
        return mapToResponse(saved);
    }

    public List<OwnerResponseDto> getAllOwners() {
        return ownerRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public OwnerResponseDto getOwnerById(Long id) {
        Owner owner = ownerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner not found with id: " + id));
        return mapToResponse(owner);
    }

    public OwnerResponseDto updateOwner(Long id, OwnerRequestDto request) {
        Owner owner = ownerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner not found with id: " + id));

        owner.setName(request.getName());
        owner.setEmail(request.getEmail());
        owner.setPhoneNumber(request.getPhoneNumber());

        Owner updated = ownerRepository.save(owner);
        return mapToResponse(updated);
    }

    public void deleteOwner(Long id) {
        Owner owner = ownerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner not found with id: " + id));
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
