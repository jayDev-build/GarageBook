package GarageBook.GarageBook.Controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import GarageBook.GarageBook.Dto.Request.CreateVehicleRequestDto;
import GarageBook.GarageBook.Dto.Request.UpdateVehicleRequestDto;
import GarageBook.GarageBook.Dto.Response.VehicleResponseDto;
import GarageBook.GarageBook.Service.VehicleService;

@RequestMapping("/vehicles")
@RestController
public class VehicleController {
    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping
    public ResponseEntity<VehicleResponseDto> createVehicle(@RequestBody CreateVehicleRequestDto request) {
        VehicleResponseDto response = vehicleService.createVehicle(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<VehicleResponseDto>> getAllVehicles() {
        List<VehicleResponseDto> response = vehicleService.getAllVehicles();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponseDto> getVehicleById(@PathVariable Long id) {
        VehicleResponseDto response = vehicleService.getVehicleById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponseDto> updateVehicle(@PathVariable Long id,
            @RequestBody UpdateVehicleRequestDto request) {
        VehicleResponseDto response = vehicleService.updateVehicle(id, request);
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }
}
