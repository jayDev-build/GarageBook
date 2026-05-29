package GarageBook.GarageBook.Controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import GarageBook.GarageBook.Dto.Request.ServiceBookingRequestDto;
import GarageBook.GarageBook.Dto.Response.ServiceBookingResponseDto;
import GarageBook.GarageBook.Service.ServiceBookingService;

@RequestMapping("/api/bookings")
@RestController
public class ServiceBookingController {
    private final ServiceBookingService serviceBookingService;

    public ServiceBookingController(ServiceBookingService serviceBookingService) {
        this.serviceBookingService = serviceBookingService;
    }

    @PostMapping
    public ResponseEntity<ServiceBookingResponseDto> createBooking(@RequestBody ServiceBookingRequestDto request) {
        ServiceBookingResponseDto response = serviceBookingService.createBooking(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ServiceBookingResponseDto>> getAllBookings() {
        List<ServiceBookingResponseDto> response = serviceBookingService.getAllBookings();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceBookingResponseDto> getBookingById(@PathVariable Long id) {
        ServiceBookingResponseDto response = serviceBookingService.getBookingById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceBookingResponseDto> updateBooking(@PathVariable Long id, @RequestBody ServiceBookingRequestDto request) {
        ServiceBookingResponseDto response = serviceBookingService.updateBooking(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        serviceBookingService.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }
}
