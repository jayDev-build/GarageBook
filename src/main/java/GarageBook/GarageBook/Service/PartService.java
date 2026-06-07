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
import GarageBook.GarageBook.Repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
public class PartService {
    private final PartRepository partRepository;
    private final UserRepository userRepository;

    public PartService(PartRepository partRepository, UserRepository userRepository) {
        this.partRepository = partRepository;
        this.userRepository = userRepository;
    }

    public PartResponseDto createPart(CreatePartRequestDto request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Garage garage = currentUser.getGarage();
        if (garage == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not associated with any garage");
        }

        Part part = Part.builder()
                .partName(request.getPartName())
                .partSize(request.getPartSize())
                .partNumber(request.getPartNumber())
                .stockQuantity(request.getStockQuantity())
                .pricePerUnit(request.getPricePerUnit())
                .garage(garage)
                .build();

        Part saved = partRepository.save(part);
        return mapToResponse(saved);
    }

    public List<PartResponseDto> getAllParts() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Garage garage = currentUser.getGarage();
        if (garage == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not associated with any garage");
        }
        List<Part> ls = partRepository.findByGarage(garage);
        return ls.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PartResponseDto getPartById(Long id) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Part part = partRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Part not found with id: " + id));

        userRepository.findByUserIdAndGarageId(currentUser.getId(), part.getGarage().getGarageId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to part: " + id));

        return mapToResponse(part);
    }

    public PartResponseDto updatePart(Long id, UpdatePartRequestDto request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Part part = partRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Part not found with id: " + id));

        userRepository.findByUserIdAndGarageId(currentUser.getId(), part.getGarage().getGarageId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to part: " + id));

        part.setPartName(request.getPartName());
        part.setPartSize(request.getPartSize());
        part.setPartNumber(request.getPartNumber());
        part.setPricePerUnit(request.getPricePerUnit());

        Part updated = partRepository.save(part);
        return mapToResponse(updated);
    }

    public void deletePart(Long id) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Part part = partRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Part not found with id: " + id));

        userRepository.findByUserIdAndGarageId(currentUser.getId(), part.getGarage().getGarageId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to part: " + id));

        partRepository.delete(part);
    }

    private PartResponseDto mapToResponse(Part part) {
        return PartResponseDto.builder()
                .partId(part.getPartId())
                .partName(part.getPartName())
                .partSize(part.getPartSize())
                .partNumber(part.getPartNumber())
                .stockQuantity(part.getStockQuantity())
                .pricePerUnit(part.getPricePerUnit())
                .garageId(part.getGarage() != null ? part.getGarage().getGarageId() : null)
                .garageName(part.getGarage() != null ? part.getGarage().getName() : null)
                .build();
    }
}
