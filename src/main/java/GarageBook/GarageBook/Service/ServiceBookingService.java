package GarageBook.GarageBook.Service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import GarageBook.GarageBook.Dto.Request.CreateServiceBookingRequestDto;
import GarageBook.GarageBook.Dto.Request.CreateServicePartRequestDto;
import GarageBook.GarageBook.Dto.Request.UpdateServiceBookingRequestDto;
import GarageBook.GarageBook.Dto.Response.ServiceBookingResponseDto;
import GarageBook.GarageBook.Dto.Response.ServicePartResponseDto;
import GarageBook.GarageBook.Models.Garage;
import GarageBook.GarageBook.Models.Invoice;
import GarageBook.GarageBook.Models.Part;
import GarageBook.GarageBook.Models.ServiceBooking;
import GarageBook.GarageBook.Models.ServicePart;
import GarageBook.GarageBook.Models.Vehicle;
import GarageBook.GarageBook.Repository.GarageRepository;
import GarageBook.GarageBook.Repository.ServiceBookingRepository;
import GarageBook.GarageBook.Repository.ServicePartRepository;

import org.springframework.transaction.annotation.Transactional;
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
    private final GarageBook.GarageBook.Repository.PartRepository partRepository;
    private final GarageBook.GarageBook.Repository.ServicePartRepository servicePartRepository;

    public ServiceBookingService(
            ServiceBookingRepository serviceBookingRepository,
            VehicleRepository vehicleRepository,
            GarageRepository garageRepository,
            GarageBook.GarageBook.Repository.PartRepository partRepository,
            ServicePartRepository servicePartRepository) {
        this.serviceBookingRepository = serviceBookingRepository;
        this.vehicleRepository = vehicleRepository;
        this.garageRepository = garageRepository;
        this.partRepository = partRepository;
        this.servicePartRepository = servicePartRepository;
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

    public ServiceBookingResponseDto createBooking(CreateServiceBookingRequestDto request) {
        Garage garage = getAuthenticatedUserGarage();
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Vehicle not found with id: " + request.getVehicleId()));

        ServiceBooking booking = ServiceBooking.builder()
                .vehicle(vehicle)
                .serviceType(request.getServiceType())
                .bookingTime(request.getBookingTime())
                .bookingStatus(request.getBookingStatus())
                .totalAmount(request.getTotalAmount())
                .labourCharges(request.getLabourCharges())
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Service booking not found with id: " + id));

        if (booking.getGarage() == null || !booking.getGarage().getGarageId().equals(garage.getGarageId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to service booking: " + id);
        }

        return mapToResponse(booking);
    }

    @Transactional // 1. Crucial for inventory rollbacks if stock check fails
    public ServiceBookingResponseDto updateBooking(Long id, UpdateServiceBookingRequestDto request) {
        Garage garage = getAuthenticatedUserGarage();
        ServiceBooking booking = serviceBookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Service booking not found with id: " + id));

        if (booking.getGarage() == null || !booking.getGarage().getGarageId().equals(garage.getGarageId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to service booking: " + id);
        }

        if (request.getServiceType() != null)
            booking.setServiceType(request.getServiceType());
        if (request.getBookingTime() != null)
            booking.setBookingTime(request.getBookingTime());
        if (request.getBookingStatus() != null)
            booking.setBookingStatus(request.getBookingStatus());
        if (request.getTotalAmount() != null)
            booking.setTotalAmount(request.getTotalAmount());
        if (request.getLabourCharges() != null)
            booking.setLabourCharges(request.getLabourCharges());

        // --- INVENTORY MANAGEMENT LOGIC START ---
        if (request.getServiceParts() != null) {

            // 1. Snapshot old state into a standalone temporary list
            // This isolates the elements so we don't cause a concurrent modification loop
            List<ServicePart> oldPartsSnapshot = new java.util.ArrayList<>(booking.getServiceParts());

            // 2. Revert Previous State using the snapshot
            for (ServicePart oldServicePart : oldPartsSnapshot) {
                Part part = oldServicePart.getPart();
                part.setStockQuantity(part.getStockQuantity() + oldServicePart.getQuantity()); // Restoring stock
                partRepository.save(part);
            }

            // 3. Clear the managed collection directly!
            // Because orphanRemoval = true is active, Hibernate marks these records for
            // deletion
            // and manages the internal tracker flawlessly without breaking references.
            booking.getServiceParts().clear();

            // Flush the restored stock updates so step 4 can read fresh numbers
            partRepository.flush();

            // 4. Apply New State: Validate and subtract new quantities
            for (CreateServicePartRequestDto partReq : request.getServiceParts()) {
                Part part = partRepository.findById(partReq.getPartId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Part not found with id: " + partReq.getPartId()));

                // Stock Check Validation
                if (part.getStockQuantity() < partReq.getQuantity()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Insufficient stock for part: " + part.getPartName() + " (Requested: "
                                    + partReq.getQuantity() + ", Available: " + part.getStockQuantity() + ")");
                }

                // Deduct from inventory
                part.setStockQuantity(part.getStockQuantity() - partReq.getQuantity());
                partRepository.save(part);

                // Construct new ServicePart mapping
                ServicePart servicePart = ServicePart.builder()
                        .part(part)
                        .serviceBooking(booking)
                        .quantity(partReq.getQuantity())
                        .pricePerUnit(partReq.getPricePerUnit())
                        .totalPrice(partReq.getPricePerUnit() * partReq.getQuantity())
                        .build();

                // Add to the managed collection (Hibernate handles the SQL INSERT later)
                booking.addServicePart(servicePart);
            }
        } // --- INVENTORY MANAGEMENT LOGIC END ---

        booking.setGarage(garage);
        ServiceBooking updated = serviceBookingRepository.save(booking);
        return mapToResponse(updated);

    }

    public void deleteBooking(Long id) {
        Garage garage = getAuthenticatedUserGarage();
        ServiceBooking booking = serviceBookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Service booking not found with id: " + id));

        if (booking.getGarage() == null || !booking.getGarage().getGarageId().equals(garage.getGarageId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to service booking: " + id);
        }

        serviceBookingRepository.delete(booking);
    }

    public Invoice getInvoice(Long bookingId) {
        ServiceBooking booking = serviceBookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Service booking not found with id: " + bookingId));

        return Invoice.builder()
                .id(booking.getId())
                .invoiceNumber("INV-" + booking.getId())
                .totalAmount(booking.getTotalAmount())
                .createdAt(booking.getBookingTime().toLocalDate())
                .garage(booking.getGarage())
                .owner(booking.getVehicle().getOwner())
                .vehicle(booking.getVehicle())
                .invoiceItems(booking.getServiceParts())
                .build();
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
                .labourCharges(booking.getLabourCharges())
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
                .serviceBookingId(
                        servicePart.getServiceBooking() != null ? servicePart.getServiceBooking().getId() : null)
                .quantity(servicePart.getQuantity())
                .pricePerUnit(servicePart.getPricePerUnit())
                .totalPrice(servicePart.getTotalPrice())
                .build();
    }
}
