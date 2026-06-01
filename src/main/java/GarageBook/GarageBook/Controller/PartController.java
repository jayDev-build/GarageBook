package GarageBook.GarageBook.Controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import GarageBook.GarageBook.Dto.Request.CreatePartRequestDto;
import GarageBook.GarageBook.Dto.Request.UpdatePartRequestDto;
import GarageBook.GarageBook.Dto.Response.PartResponseDto;
import GarageBook.GarageBook.Service.PartService;

@RequestMapping("/api/parts")
@RestController
public class PartController {
    private final PartService partService;

    public PartController(PartService partService) {
        this.partService = partService;
    }

    @PostMapping
    public ResponseEntity<PartResponseDto> createPart(@RequestBody CreatePartRequestDto request) {
        PartResponseDto response = partService.createPart(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<PartResponseDto>> getAllParts() {
        List<PartResponseDto> response = partService.getAllParts();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PartResponseDto> getPartById(@PathVariable Long id) {
        PartResponseDto response = partService.getPartById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PartResponseDto> updatePart(@PathVariable Long id, @RequestBody UpdatePartRequestDto request) {
        PartResponseDto response = partService.updatePart(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePart(@PathVariable Long id) {
        partService.deletePart(id);
        return ResponseEntity.noContent().build();
    }
}
