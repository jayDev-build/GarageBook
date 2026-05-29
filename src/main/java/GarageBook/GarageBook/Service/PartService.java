package GarageBook.GarageBook.Service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import GarageBook.GarageBook.Dto.Request.PartRequestDto;
import GarageBook.GarageBook.Dto.Response.PartResponseDto;
import GarageBook.GarageBook.Models.Part;
import GarageBook.GarageBook.Repository.PartRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PartService {
    private final PartRepository partRepository;

    public PartService(PartRepository partRepository) {
        this.partRepository = partRepository;
    }

    public PartResponseDto createPart(PartRequestDto request) {
        Part part = Part.builder()
                .partName(request.getPartName())
                .partSize(request.getPartSize())
                .partNumber(request.getPartNumber())
                .stockQuantity(request.getStockQuantity())
                .defaultPrice(request.getDefaultPrice())   
                .build();

        Part saved = partRepository.save(part);
        return mapToResponse(saved);
    }

    public List<PartResponseDto> getAllParts() {
        return partRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PartResponseDto getPartById(Long id) {
        Part part = partRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Part not found with id: " + id));
        return mapToResponse(part);
    }

    public PartResponseDto updatePart(Long id, PartRequestDto request) {
        Part part = partRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Part not found with id: " + id));

        part.setPartName(request.getPartName());
        part.setPartSize(request.getPartSize());
        part.setPartNumber(request.getPartNumber());
        part.setStockQuantity(request.getStockQuantity());
        part.setDefaultPrice(request.getDefaultPrice());

        Part updated = partRepository.save(part);
        return mapToResponse(updated);
    }

    public void deletePart(Long id) {
        Part part = partRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Part not found with id: " + id));
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
                .build();
    }
}
