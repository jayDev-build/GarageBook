package GarageBook.GarageBook.Controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import GarageBook.GarageBook.Dto.Request.CreateOwnerRequestDto;
import GarageBook.GarageBook.Dto.Request.UpdateOwnerRequestDto;
import GarageBook.GarageBook.Dto.Response.OwnerResponseDto;
import GarageBook.GarageBook.Service.OwnerService;

@RequestMapping("/api/owners")
@RestController
public class OwnerController {
    private final OwnerService ownerService;

    public OwnerController(OwnerService ownerService) {
        this.ownerService = ownerService;
    }

    @PostMapping
    public ResponseEntity<OwnerResponseDto> createOwner(@RequestBody CreateOwnerRequestDto request) {
        OwnerResponseDto response = ownerService.createOwner(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<OwnerResponseDto>> getAllOwners() {
        List<OwnerResponseDto> response = ownerService.getAllOwners();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OwnerResponseDto> getOwnerById(@PathVariable Long id) {
        OwnerResponseDto response = ownerService.getOwnerById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OwnerResponseDto> updateOwner(@PathVariable Long id, @RequestBody UpdateOwnerRequestDto request) {
        OwnerResponseDto response = ownerService.updateOwner(id, request);
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOwner(@PathVariable Long id) {
        ownerService.deleteOwner(id);
        return ResponseEntity.noContent().build();
    }
}
