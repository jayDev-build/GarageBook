package GarageBook.GarageBook.Controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import GarageBook.GarageBook.Dto.Request.ServicePartRequestDto;
import GarageBook.GarageBook.Dto.Response.ServicePartResponseDto;
import GarageBook.GarageBook.Service.ServicePartService;

@RequestMapping("/api/service-parts")
@RestController
public class ServicePartController {
    private final ServicePartService servicePartService;

    public ServicePartController(ServicePartService servicePartService) {
        this.servicePartService = servicePartService;
    }

    @PostMapping
    public ResponseEntity<ServicePartResponseDto> createServicePart(@RequestBody ServicePartRequestDto request) {
        ServicePartResponseDto response = servicePartService.createServicePart(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ServicePartResponseDto>> getAllServiceParts() {
        List<ServicePartResponseDto> response = servicePartService.getAllServiceParts();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServicePartResponseDto> getServicePartById(@PathVariable Long id) {
        ServicePartResponseDto response = servicePartService.getServicePartById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServicePartResponseDto> updateServicePart(@PathVariable Long id, @RequestBody ServicePartRequestDto request) {
        ServicePartResponseDto response = servicePartService.updateServicePart(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteServicePart(@PathVariable Long id) {
        servicePartService.deleteServicePart(id);
        return ResponseEntity.noContent().build();
    }
}
