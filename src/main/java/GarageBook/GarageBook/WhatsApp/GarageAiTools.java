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

@Component
public class GarageAiTools {

    private final AppointmentRepository appointmentRepository;
    private final CustomerRepository customerRepository;
    private final GarageRepository garageRepository;

    public GarageAiTools(
            AppointmentRepository appointmentRepository,
            CustomerRepository customerRepository,
            GarageRepository garageRepository) {
        this.appointmentRepository = appointmentRepository;
        this.customerRepository = customerRepository;
        this.garageRepository = garageRepository;
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
                .map(a -> a.getAppointmentTime().toLocalTime().toString().substring(0, 5))
                .toList();

            return allSlots.stream()
                .filter(slot -> !bookedSlots.contains(slot))
                .toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    @Tool(description = "Book a new appointment at a specific garage for a customer on a given date-time (format: YYYY-MM-DDTHH:MM).")
    public String bookAppointment(
            Long garageId, 
            String customerPhoneNumber, 
            String customerName, 
            String appointmentTime, 
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
            return "Appointment successfully booked. Booking ID: " + saved.getId();
        } catch (Exception e) {
            return "Failed to book appointment: " + e.getMessage();
        }
    }
}
