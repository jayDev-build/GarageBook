package GarageBook.GarageBook.Service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import GarageBook.GarageBook.Dto.Request.ServicePartRequestDto;
import GarageBook.GarageBook.Dto.Response.ServicePartResponseDto;
import GarageBook.GarageBook.Models.Garage;
import GarageBook.GarageBook.Models.Part;
import GarageBook.GarageBook.Models.ServiceBooking;
import GarageBook.GarageBook.Models.ServicePart;
import GarageBook.GarageBook.Repository.PartRepository;
import GarageBook.GarageBook.Repository.ServiceBookingRepository;
import GarageBook.GarageBook.Repository.ServicePartRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import GarageBook.GarageBook.Models.User;

@Service
public class ServicePartService {
    private final ServicePartRepository servicePartRepository;
    private final PartRepository partRepository;
    private final ServiceBookingRepository serviceBookingRepository;

    public ServicePartService(
            ServicePartRepository servicePartRepository,
            PartRepository partRepository,
            ServiceBookingRepository serviceBookingRepository) {
        this.servicePartRepository = servicePartRepository;
        this.partRepository = partRepository;
        this.serviceBookingRepository = serviceBookingRepository;
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

    public ServicePartResponseDto createServicePart(ServicePartRequestDto request) {
        Garage garage = getAuthenticatedUserGarage();
        Part part = partRepository.findById(request.getPartId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Part not found with id: " + request.getPartId()));

        ServiceBooking booking = serviceBookingRepository.findById(request.getServiceBookingId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service booking not found with id: " + request.getServiceBookingId()));

        if (booking.getGarage() == null || !booking.getGarage().getGarageId().equals(garage.getGarageId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to service booking: " + request.getServiceBookingId());
        }

        long calculatedTotalPrice = (long) request.getQuantity() * request.getPricePerUnit();

        ServicePart servicePart = ServicePart.builder()
                .part(part)
                .serviceBooking(booking)
                .quantity(request.getQuantity())
                .pricePerUnit(request.getPricePerUnit())
                .totalPrice(calculatedTotalPrice)
                .build();

        booking.addServicePart(servicePart);
        ServicePart saved = servicePartRepository.save(servicePart);
        
        updateBookingTotal(booking);

        return mapToResponse(saved);
    }

    public List<ServicePartResponseDto> getAllServiceParts() {
        Garage garage = getAuthenticatedUserGarage();
        return servicePartRepository.findAll().stream()
                .filter(sp -> sp.getServiceBooking() != null && sp.getServiceBooking().getGarage() != null && sp.getServiceBooking().getGarage().getGarageId().equals(garage.getGarageId()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ServicePartResponseDto getServicePartById(Long id) {
        Garage garage = getAuthenticatedUserGarage();
        ServicePart servicePart = servicePartRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ServicePart not found with id: " + id));

        if (servicePart.getServiceBooking() == null || servicePart.getServiceBooking().getGarage() == null 
                || !servicePart.getServiceBooking().getGarage().getGarageId().equals(garage.getGarageId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to service part: " + id);
        }

        return mapToResponse(servicePart);
    }

    public ServicePartResponseDto updateServicePart(Long id, ServicePartRequestDto request) {
        Garage garage = getAuthenticatedUserGarage();
        ServicePart servicePart = servicePartRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ServicePart not found with id: " + id));

        if (servicePart.getServiceBooking() == null || servicePart.getServiceBooking().getGarage() == null 
                || !servicePart.getServiceBooking().getGarage().getGarageId().equals(garage.getGarageId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to service part: " + id);
        }

        Part part = partRepository.findById(request.getPartId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Part not found with id: " + request.getPartId()));

        long calculatedTotalPrice = (long) request.getQuantity() * request.getPricePerUnit();

        servicePart.setPart(part);
        servicePart.setQuantity(request.getQuantity());
        servicePart.setPricePerUnit(request.getPricePerUnit());
        servicePart.setTotalPrice(calculatedTotalPrice);

        ServicePart updated = servicePartRepository.save(servicePart);
        
        if (servicePart.getServiceBooking() != null) {
            updateBookingTotal(servicePart.getServiceBooking());
        }

        return mapToResponse(updated);
    }

    public void deleteServicePart(Long id) {
        Garage garage = getAuthenticatedUserGarage();
        ServicePart servicePart = servicePartRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ServicePart not found with id: " + id));
        
        if (servicePart.getServiceBooking() == null || servicePart.getServiceBooking().getGarage() == null 
                || !servicePart.getServiceBooking().getGarage().getGarageId().equals(garage.getGarageId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to service part: " + id);
        }

        ServiceBooking booking = servicePart.getServiceBooking();
        if (booking != null) {
            booking.removeServicePart(servicePart);
        }

        servicePartRepository.delete(servicePart);

        if (booking != null) {
            updateBookingTotal(booking);
        }
    }

    private void updateBookingTotal(ServiceBooking booking) {
        long sum = booking.getServiceParts().stream()
                .mapToLong(sp -> sp.getTotalPrice() != null ? sp.getTotalPrice() : 0L)
                .sum();
        booking.setTotalAmount(sum);
        serviceBookingRepository.save(booking);
    }

    private ServicePartResponseDto mapToResponse(ServicePart servicePart) {
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
