package GarageBook.GarageBook.Service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import GarageBook.GarageBook.Dto.Request.CreatePartRequestDto;
import GarageBook.GarageBook.Dto.Request.UpdatePartRequestDto;
import GarageBook.GarageBook.Dto.Response.PartResponseDto;
import GarageBook.GarageBook.Models.Part;
import GarageBook.GarageBook.Models.Garage;
import GarageBook.GarageBook.Models.User;
import GarageBook.GarageBook.Repository.PartRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
public class PartService {
    private final PartRepository partRepository;

    public PartService(PartRepository partRepository) {
        this.partRepository = partRepository;
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

    public PartResponseDto createPart(CreatePartRequestDto request) {
        Garage garage = getAuthenticatedUserGarage();

        Part part = Part.builder()
                .partName(request.getPartName())
                .partSize(request.getPartSize())
                .partNumber(request.getPartNumber())
                .stockQuantity(request.getStockQuantity())
                .defaultPrice(request.getDefaultPrice())   
                .garage(garage)
                .build();

        Part saved = partRepository.save(part);
        return mapToResponse(saved);
    }

    public List<PartResponseDto> getAllParts() {
        Garage garage = getAuthenticatedUserGarage();
        return partRepository.findByGarage(garage).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PartResponseDto getPartById(Long id) {
        Garage garage = getAuthenticatedUserGarage();
        Part part = partRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Part not found with id: " + id));

        if (part.getGarage() == null || !part.getGarage().getGarageId().equals(garage.getGarageId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to part: " + id);
        }

        return mapToResponse(part);
    }

    public PartResponseDto updatePart(Long id, UpdatePartRequestDto request) {
        Garage garage = getAuthenticatedUserGarage();
        Part part = partRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Part not found with id: " + id));

        if (part.getGarage() == null || !part.getGarage().getGarageId().equals(garage.getGarageId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to part: " + id);
        }

        part.setPartName(request.getPartName());
        part.setPartSize(request.getPartSize());
        part.setPartNumber(request.getPartNumber());
        part.setDefaultPrice(request.getDefaultPrice());
        part.setGarage(garage);

        Part updated = partRepository.save(part);
        return mapToResponse(updated);
    }


    public void deletePart(Long id) {
        Garage garage = getAuthenticatedUserGarage();
        Part part = partRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Part not found with id: " + id));

        if (part.getGarage() == null || !part.getGarage().getGarageId().equals(garage.getGarageId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to part: " + id);
        }

        partRepository.delete(part);
    }

    private PartResponseDto mapToResponse(Part part) {
        return PartResponseDto.builder()
                .partId(part.getPartId())
                .partName(part.getPartName())
                .partSize(part.getPartSize())
                .partNumber(part.getPartNumber())
                .stockQuantity(part.getStockQuantity())
                .defaultPrice(part.getDefaultPrice())
                .garageId(part.getGarage() != null ? part.getGarage().getGarageId() : null)
                .garageName(part.getGarage() != null ? part.getGarage().getName() : null)
                .build();
    }
}
