package GarageBook.GarageBook.Controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import GarageBook.GarageBook.Dto.Request.CreateGarageRequestDto;
import GarageBook.GarageBook.Dto.Request.UpdateGarageRequestDto;
import GarageBook.GarageBook.Dto.Response.GarageResponseDto;
import GarageBook.GarageBook.Service.GarageService;

@RequestMapping("/api/garages")
@RestController
public class GarageController {
    private final GarageService garageService;

    public GarageController(GarageService garageService) {
        this.garageService = garageService;
    }

    @PostMapping
    public ResponseEntity<GarageResponseDto> createGarage(@RequestBody CreateGarageRequestDto request) {
        GarageResponseDto response = garageService.createGarage(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<GarageResponseDto>> getAllGarages() {
        List<GarageResponseDto> response = garageService.getAllGarages();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GarageResponseDto> getGarageById(@PathVariable Long id) {
        GarageResponseDto response = garageService.getGarageById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GarageResponseDto> updateGarage(@PathVariable Long id, @RequestBody UpdateGarageRequestDto request) {
        GarageResponseDto response = garageService.updateGarage(id, request);
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGarage(@PathVariable Long id) {
        garageService.deleteGarage(id);
        return ResponseEntity.noContent().build();
    }
}
