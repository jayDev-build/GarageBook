package GarageBook.GarageBook.Controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import GarageBook.GarageBook.Dto.Request.MechanicRequestDto;
import GarageBook.GarageBook.Dto.Response.MechanicResponseDto;
import GarageBook.GarageBook.Service.MechanicService;

@RequestMapping("/api/mechanics")
@RestController
public class MechanicController {
    private final MechanicService mechanicService;

    public MechanicController(MechanicService mechanicService) {
        this.mechanicService = mechanicService;
    }

    @PostMapping
    public ResponseEntity<MechanicResponseDto> createMechanic(@RequestBody MechanicRequestDto request) {
        MechanicResponseDto response = mechanicService.createMechanic(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<MechanicResponseDto>> getAllMechanics() {
        List<MechanicResponseDto> response = mechanicService.getAllMechanics();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MechanicResponseDto> getMechanicById(@PathVariable Long id) {
        MechanicResponseDto response = mechanicService.getMechanicById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MechanicResponseDto> updateMechanic(@PathVariable Long id, @RequestBody MechanicRequestDto request) {
        MechanicResponseDto response = mechanicService.updateMechanic(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMechanic(@PathVariable Long id) {
        mechanicService.deleteMechanic(id);
        return ResponseEntity.noContent().build();
    }
}
