package GarageBook.GarageBook.Service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import GarageBook.GarageBook.Dto.Request.ServiceBookingRequestDto;
import GarageBook.GarageBook.Dto.Response.ServiceBookingResponseDto;
import GarageBook.GarageBook.Dto.Response.ServicePartResponseDto;
import GarageBook.GarageBook.Models.Garage;
import GarageBook.GarageBook.Models.ServiceBooking;
import GarageBook.GarageBook.Models.ServicePart;
import GarageBook.GarageBook.Models.Vehicle;
import GarageBook.GarageBook.Repository.GarageRepository;
import GarageBook.GarageBook.Repository.ServiceBookingRepository;
import GarageBook.GarageBook.Repository.VehicleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import GarageBook.GarageBook.Models.User;

@Service
public class ServiceBookingService {
    private final ServiceBookingRepository serviceBookingRepository;
    private final VehicleRepository vehicleRepository;
    private final GarageRepository garageRepository;

    public ServiceBookingService(
            ServiceBookingRepository serviceBookingRepository,
            VehicleRepository vehicleRepository,
            GarageRepository garageRepository) {
        this.serviceBookingRepository = serviceBookingRepository;
        this.vehicleRepository = vehicleRepository;
        this.garageRepository = garageRepository;
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

    public ServiceBookingResponseDto createBooking(ServiceBookingRequestDto request) {
        Garage garage = getAuthenticatedUserGarage();
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found with id: " + request.getVehicleId()));

        ServiceBooking booking = ServiceBooking.builder()
                .vehicle(vehicle)
                .serviceType(request.getServiceType())
                .bookingTime(request.getBookingTime())
                .bookingStatus(request.getBookingStatus())
                .totalAmount(request.getTotalAmount())
                .garage(garage)
                .build();

        ServiceBooking saved = serviceBookingRepository.save(booking);
        return mapToResponse(saved);
    }

    public List<ServiceBookingResponseDto> getAllBookings() {
        Garage garage = getAuthenticatedUserGarage();
        return serviceBookingRepository.findByGarage(garage).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ServiceBookingResponseDto getBookingById(Long id) {
        Garage garage = getAuthenticatedUserGarage();
        ServiceBooking booking = serviceBookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service booking not found with id: " + id));

        if (booking.getGarage() == null || !booking.getGarage().getGarageId().equals(garage.getGarageId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to service booking: " + id);
        }

        return mapToResponse(booking);
    }

    public ServiceBookingResponseDto updateBooking(Long id, ServiceBookingRequestDto request) {
        Garage garage = getAuthenticatedUserGarage();
        ServiceBooking booking = serviceBookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service booking not found with id: " + id));

        if (booking.getGarage() == null || !booking.getGarage().getGarageId().equals(garage.getGarageId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to service booking: " + id);
        }

        if (request.getVehicleId() != null) {
            Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found with id: " + request.getVehicleId()));
            booking.setVehicle(vehicle);
        }

        if (request.getServiceType() != null) {
            booking.setServiceType(request.getServiceType());
        }

        if (request.getBookingTime() != null) {
            booking.setBookingTime(request.getBookingTime());
        }

        if (request.getBookingStatus() != null) {
            booking.setBookingStatus(request.getBookingStatus());
        }

        if (request.getTotalAmount() != null) {
            booking.setTotalAmount(request.getTotalAmount());
        }

        booking.setGarage(garage);

        ServiceBooking updated = serviceBookingRepository.save(booking);
        return mapToResponse(updated);
    }

    public void deleteBooking(Long id) {
        Garage garage = getAuthenticatedUserGarage();
        ServiceBooking booking = serviceBookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service booking not found with id: " + id));

        if (booking.getGarage() == null || !booking.getGarage().getGarageId().equals(garage.getGarageId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to service booking: " + id);
        }

        serviceBookingRepository.delete(booking);
    }

    private ServiceBookingResponseDto mapToResponse(ServiceBooking booking) {
        List<ServicePartResponseDto> partsList = booking.getServiceParts() != null 
                ? booking.getServiceParts().stream()
                        .map(this::mapToServicePartResponse)
                        .collect(Collectors.toList())
                : List.of();

        return ServiceBookingResponseDto.builder()
                .id(booking.getId())
                .vehicleId(booking.getVehicle().getId())
                .vehicleNumber(booking.getVehicle().getVehicleNumber())
                .serviceType(booking.getServiceType())
                .bookingTime(booking.getBookingTime())
                .bookingStatus(booking.getBookingStatus())
                .totalAmount(booking.getTotalAmount())
                .garageId(booking.getGarage() != null ? booking.getGarage().getGarageId() : null)
                .garageName(booking.getGarage() != null ? booking.getGarage().getName() : null)
                .serviceParts(partsList)
                .build();
    }

    private ServicePartResponseDto mapToServicePartResponse(ServicePart servicePart) {
        return ServicePartResponseDto.builder()
                .id(servicePart.getId())
                .partId(servicePart.getPart() != null ? servicePart.getPart().getPartId() : null)
                .partName(servicePart.getPart() != null ? servicePart.getPart().getPartName() : null)
                .serviceBookingId(servicePart.getServiceBooking() != null ? servicePart.getServiceBooking().getId() : null)
                .quantity(servicePart.getQuantity())
                .pricePerUnit(servicePart.getPricePerUnit())
                .totalPrice(servicePart.getTotalPrice())
                .build();
    }
}
