package GarageBook.GarageBook.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
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
import GarageBook.GarageBook.Repository.UserRepository;
import GarageBook.GarageBook.Enums.BookingStatus;
import org.springframework.transaction.annotation.Transactional;
import GarageBook.GarageBook.Repository.VehicleRepository;
import GarageBook.GarageBook.WhatsApp.WhatsAppNotificationService;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.context.SecurityContextHolder;
import GarageBook.GarageBook.Models.User;

@Service
public class ServiceBookingService {
    private final ServiceBookingRepository serviceBookingRepository;
    private final VehicleRepository vehicleRepository;
    private final GarageRepository garageRepository;
    private final GarageBook.GarageBook.Repository.PartRepository partRepository;
    private final GarageBook.GarageBook.Repository.ServicePartRepository servicePartRepository;
    private final WhatsAppNotificationService whatsAppNotificationService;
    private final UserRepository userRepository;

    public ServiceBookingService(
            ServiceBookingRepository serviceBookingRepository,
            VehicleRepository vehicleRepository,
            GarageRepository garageRepository,
            GarageBook.GarageBook.Repository.PartRepository partRepository,
            ServicePartRepository servicePartRepository,
            WhatsAppNotificationService whatsAppNotificationService,
            UserRepository userRepository) {
        this.serviceBookingRepository = serviceBookingRepository;
        this.vehicleRepository = vehicleRepository;
        this.garageRepository = garageRepository;
        this.partRepository = partRepository;
        this.servicePartRepository = servicePartRepository;
        this.whatsAppNotificationService = whatsAppNotificationService;
        this.userRepository = userRepository;
    }

    public ServiceBookingResponseDto createBooking(CreateServiceBookingRequestDto request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Garage garage = currentUser.getGarage();
        if (garage == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not associated with any garage");
        }

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
        whatsAppNotificationForCreateService(saved);
        return mapToResponse(saved);
    }

    public List<ServiceBookingResponseDto> getAllBookings() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Garage garage = currentUser.getGarage();
        if (garage == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not associated with any garage");
        }
        return serviceBookingRepository.findByGarage(garage).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ServiceBookingResponseDto getBookingById(Long id) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ServiceBooking booking = serviceBookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Service booking not found with id: " + id));

        userRepository.findByUserIdAndGarageId(currentUser.getId(), booking.getGarage().getGarageId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Access denied to service booking: " + id));

        return mapToResponse(booking);
    }

    @Transactional
    public ServiceBookingResponseDto updateBooking(Long id, UpdateServiceBookingRequestDto request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ServiceBooking booking = serviceBookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Service booking not found with id: " + id));

        userRepository.findByUserIdAndGarageId(currentUser.getId(), booking.getGarage().getGarageId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Access denied to service booking: " + id));

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

        if (request.getServiceParts() != null) {
            List<ServicePart> oldPartsSnapshot = new java.util.ArrayList<>(booking.getServiceParts());

            for (ServicePart oldServicePart : oldPartsSnapshot) {
                Part part = oldServicePart.getPart();
                part.setStockQuantity(part.getStockQuantity() + oldServicePart.getQuantity());
                partRepository.save(part);
            }

            booking.getServiceParts().clear();
            partRepository.flush();

            for (CreateServicePartRequestDto partReq : request.getServiceParts()) {
                Part part = partRepository.findById(partReq.getPartId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Part not found with id: " + partReq.getPartId()));

                if (part.getStockQuantity() < partReq.getQuantity()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Insufficient stock for part: " + part.getPartName() + " (Requested: "
                                    + partReq.getQuantity() + ", Available: " + part.getStockQuantity() + ")");
                }

                part.setStockQuantity(part.getStockQuantity() - partReq.getQuantity());
                partRepository.save(part);

                ServicePart servicePart = ServicePart.builder()
                        .part(part)
                        .serviceBooking(booking)
                        .quantity(partReq.getQuantity())
                        .pricePerUnit(partReq.getPricePerUnit())
                        .totalPrice(partReq.getPricePerUnit() * partReq.getQuantity())
                        .build();

                booking.addServicePart(servicePart);
            }
        }

        ServiceBooking updated = serviceBookingRepository.save(booking);

        if (request.getBookingStatus() != null && request.getBookingStatus().equals(BookingStatus.COMPLETED)) {
            whatsAppNotificationForCompleteService(updated);
        }

        return mapToResponse(updated);
    }

    public void deleteBooking(Long id) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ServiceBooking booking = serviceBookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Service booking not found with id: " + id));

        userRepository.findByUserIdAndGarageId(currentUser.getId(), booking.getGarage().getGarageId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Access denied to service booking: " + id));

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

    private void whatsAppNotificationForCompleteService(ServiceBooking service) {
        String ownerName = service.getVehicle().getOwner().getName();
        String vehicleNo = service.getVehicle().getVehicleNumber();
        double totalAmount = service.getTotalAmount();
        String garageAddress = service.getGarage().getAddress();
        String garagePhone = service.getGarage().getPhoneNumber();
        String ownerPhone = service.getVehicle().getOwner().getPhoneNumber();

        String templateName = "service_completed";
        String languageCode = "en";
        List<String> bodyValues = new ArrayList<>();
        bodyValues.add(ownerName);
        bodyValues.add(vehicleNo);
        bodyValues.add(String.valueOf(totalAmount));
        bodyValues.add(garageAddress);
        bodyValues.add(garagePhone);

        whatsAppNotificationService.sendTemplateNotification(ownerPhone, templateName, languageCode, bodyValues);
    }

    private void whatsAppNotificationForCreateService(ServiceBooking service) {
        String ownerName = service.getVehicle().getOwner().getName();
        String vehicleNo = service.getVehicle().getVehicleNumber();
        String garageAddress = service.getGarage().getAddress();
        String garageName = service.getGarage().getName();
        String ownerPhone = service.getVehicle().getOwner().getPhoneNumber();

        String templateName = "service_creation";
        String languageCode = "en";
        List<String> bodyValues = new ArrayList<>();
        bodyValues.add(ownerName);
        bodyValues.add(vehicleNo);
        bodyValues.add(garageName);
        bodyValues.add(garageAddress);

        whatsAppNotificationService.sendTemplateNotification(ownerPhone, templateName, languageCode, bodyValues);
    }
}
