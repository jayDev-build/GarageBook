package GarageBook.GarageBook.Service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import GarageBook.GarageBook.Dto.Request.CreateServicePartRequestDto;
import GarageBook.GarageBook.Dto.Request.UpdateServicePartRequestDto;
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

    public ServicePartResponseDto createServicePart(CreateServicePartRequestDto request) {
        Garage garage = getAuthenticatedUserGarage();
        Part part = partRepository.findById(request.getPartId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Part not found with id: " + request.getPartId()));

        ServiceBooking booking = serviceBookingRepository.findById(request.getServiceBookingId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service booking not found with id: " + request.getServiceBookingId()));

        if (booking.getGarage() == null || !booking.getGarage().getGarageId().equals(garage.getGarageId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to service booking: " + request.getServiceBookingId());
        }

        if (part.getStockQuantity() < request.getQuantity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient stock. Only " + part.getStockQuantity() + " items remaining.");
        }

        // Deduct from catalog inventory stock
        part.setStockQuantity(part.getStockQuantity() - request.getQuantity());
        partRepository.save(part);

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

    public ServicePartResponseDto updateServicePart(Long id, UpdateServicePartRequestDto request) {
        Garage garage = getAuthenticatedUserGarage();
        ServicePart servicePart = servicePartRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ServicePart not found with id: " + id));

        if (servicePart.getServiceBooking() == null || servicePart.getServiceBooking().getGarage() == null 
                || !servicePart.getServiceBooking().getGarage().getGarageId().equals(garage.getGarageId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to service part: " + id);
        }

        Part part = servicePart.getPart();
        
        // If the part is being switched to a different part
        if (request.getPartId() != null && !request.getPartId().equals(part.getPartId())) {
            Part newPart = partRepository.findById(request.getPartId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Part not found with id: " + request.getPartId()));

            int targetQty = request.getQuantity() != null ? request.getQuantity() : servicePart.getQuantity();

            // 1. Check stock of new part
            if (newPart.getStockQuantity() < targetQty) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient stock on new part. Only " + newPart.getStockQuantity() + " items remaining.");
            }

            // 2. Restock old part
            part.setStockQuantity(part.getStockQuantity() + servicePart.getQuantity());
            partRepository.save(part);

            // 3. Deduct from new part
            newPart.setStockQuantity(newPart.getStockQuantity() - targetQty);
            partRepository.save(newPart);

            part = newPart;
            if (request.getQuantity() != null) {
                servicePart.setQuantity(request.getQuantity());
            }
        } 
        // If the part stays the same but the quantity is changed
        else if (request.getQuantity() != null && !request.getQuantity().equals(servicePart.getQuantity())) {
            int diff = request.getQuantity() - servicePart.getQuantity();
            if (part.getStockQuantity() < diff) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient stock. Only " + part.getStockQuantity() + " items remaining.");
            }
            part.setStockQuantity(part.getStockQuantity() - diff);
            partRepository.save(part);

            servicePart.setQuantity(request.getQuantity());
        }

        if (request.getPricePerUnit() != null) {
            servicePart.setPricePerUnit(request.getPricePerUnit());
        }

        long calculatedTotalPrice = (long) servicePart.getQuantity() * servicePart.getPricePerUnit();

        servicePart.setPart(part);
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

        // Restock catalog inventory stock
        Part part = servicePart.getPart();
        if (part != null) {
            part.setStockQuantity(part.getStockQuantity() + servicePart.getQuantity());
            partRepository.save(part);
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
