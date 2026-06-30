package GarageBook.GarageBook.WhatsApp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import GarageBook.GarageBook.Models.Appointment;
import GarageBook.GarageBook.Models.Customer;
import GarageBook.GarageBook.Models.Garage;
import GarageBook.GarageBook.Repository.AppointmentRepository;
import GarageBook.GarageBook.Repository.CustomerRepository;
import GarageBook.GarageBook.Repository.GarageRepository;
import GarageBook.GarageBook.Repository.ServiceBookingRepository;
import GarageBook.GarageBook.Repository.OwnerRepository;
import GarageBook.GarageBook.Repository.VehicleRepository;
import GarageBook.GarageBook.Models.ServiceBooking;
import GarageBook.GarageBook.Models.Owner;
import GarageBook.GarageBook.Models.Vehicle;
import GarageBook.GarageBook.Enums.ServiceType;
import GarageBook.GarageBook.Enums.BookingStatus;
import GarageBook.GarageBook.Enums.VehicleType;

@Component
public class GarageAiTools {

    private final AppointmentRepository appointmentRepository;
    private final CustomerRepository customerRepository;
    private final GarageRepository garageRepository;
    private final ServiceBookingRepository serviceBookingRepository;
    private final OwnerRepository ownerRepository;
    private final VehicleRepository vehicleRepository;

    public GarageAiTools(
            AppointmentRepository appointmentRepository,
            CustomerRepository customerRepository,
            GarageRepository garageRepository,
            ServiceBookingRepository serviceBookingRepository,
            OwnerRepository ownerRepository,
            VehicleRepository vehicleRepository) {
        this.appointmentRepository = appointmentRepository;
        this.customerRepository = customerRepository;
        this.garageRepository = garageRepository;
        this.serviceBookingRepository = serviceBookingRepository;
        this.ownerRepository = ownerRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Tool(description = "Check available appointment slots for a given garage on a specific date (date format: YYYY-MM-DD).")
    public List<String> checkAvailableSlots(Long garageId, String date) {
        try {
            LocalDate localDate = LocalDate.parse(date);
            LocalDateTime startOfDay = localDate.atStartOfDay();
            LocalDateTime endOfDay = localDate.atTime(LocalTime.MAX);

            List<Appointment> existing = appointmentRepository.findByGarageGarageIdAndAppointmentTimeBetween(
                garageId, startOfDay, endOfDay
            );

            List<String> allSlots = List.of("09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00");
            List<String> bookedSlots = existing.stream()
                .filter(a -> !"CANCELLED".equalsIgnoreCase(a.getStatus()) && !"COMPLETED".equalsIgnoreCase(a.getStatus()))
                .map(a -> a.getAppointmentTime().toLocalTime().toString().substring(0, 5))
                .toList();

            return allSlots.stream()
                .filter(slot -> !bookedSlots.contains(slot))
                .toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    @Tool(description = "Book a new appointment at a specific garage for a customer on a given date-time (format: YYYY-MM-DDTHH:MM), specifying a serviceType (e.g. GENERAL_SERVICE, WASH, REPAIR, OIL_CHANGE) and symptoms.")
    public String bookAppointment(
            Long garageId, 
            String customerPhoneNumber, 
            String customerName, 
            String appointmentTime, 
            String serviceType,
            String symptoms) {
        try {
            Garage garage = garageRepository.findById(garageId)
                .orElseThrow(() -> new RuntimeException("Garage not found"));

            Customer customer = customerRepository.findByPhoneNumberAndGarageGarageId(
                customerPhoneNumber, garageId
            ).orElseGet(() -> {
                Customer newCust = Customer.builder()
                    .name(customerName)
                    .phoneNumber(customerPhoneNumber)
                    .garage(garage)
                    .build();
                return customerRepository.save(newCust);
            });

            LocalDateTime appTime = LocalDateTime.parse(appointmentTime);

            Appointment appointment = Appointment.builder()
                .customer(customer)
                .appointmentTime(appTime)
                .status("CONFIRMED")
                .symptoms(symptoms)
                .garage(garage)
                .build();

            Appointment saved = appointmentRepository.save(appointment);

            // Resolve or create Owner and Vehicle to associate with the Service Booking Card
            Owner owner = ownerRepository.findByPhoneNumber(customerPhoneNumber)
                .orElseGet(() -> {
                    Owner newOwner = Owner.builder()
                        .name(customerName)
                        .phoneNumber(customerPhoneNumber)
                        .garage(garage)
                        .build();
                    return ownerRepository.save(newOwner);
                });

            Vehicle vehicle;
            List<Vehicle> ownerVehicles = owner.getVehicles();
            if (ownerVehicles != null && !ownerVehicles.isEmpty()) {
                vehicle = ownerVehicles.get(0);
            } else {
                String cleanPhone = customerPhoneNumber.replaceAll("[^0-9]", "");
                String tempPlate = "WA-" + (cleanPhone.length() > 10 ? cleanPhone.substring(cleanPhone.length() - 10) : cleanPhone);
                if (tempPlate.length() > 20) {
                    tempPlate = tempPlate.substring(0, 20);
                }

                Vehicle newVehicle = Vehicle.builder()
                    .owner(owner)
                    .vehicleNumber(tempPlate)
                    .vehicleType(VehicleType.CAR)
                    .garage(garage)
                    .build();
                vehicle = vehicleRepository.save(newVehicle);
            }

            // Map serviceType parameter to ServiceType enum
            ServiceType sType = ServiceType.GENERAL_SERVICE;
            if (serviceType != null) {
                try {
                    sType = ServiceType.valueOf(serviceType.toUpperCase().trim().replace(" ", "_"));
                } catch (Exception e) {
                    // Fallback to GENERAL_SERVICE
                }
            }

            // Create corresponding ServiceBooking card (workshop job card)
            ServiceBooking serviceBooking = ServiceBooking.builder()
                .vehicle(vehicle)
                .serviceType(sType)
                .bookingTime(appTime)
                .bookingStatus(BookingStatus.CREATED)
                .totalAmount(0L)
                .labourCharges(0L)
                .garage(garage)
                .build();
            serviceBookingRepository.save(serviceBooking);

            return "Appointment successfully booked and workshop service booking card created. Booking ID: " + saved.getId();
        } catch (Exception e) {
            return "Failed to book appointment: " + e.getMessage();
        }
    }
}
