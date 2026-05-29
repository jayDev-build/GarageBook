package GarageBook.GarageBook.Enums;

public enum BookingStatus {
    CREATED("CREATED", 1),
    IN_SERVICE("IN_SERVICE", 2),
    COMPLETED("COMPLETED", 3),
    CANCELLED("CANCELLED", 4);

    private String status;
    private int code;

    BookingStatus(String status, int code) {
        this.status = status;
        this.code = code;
    }

    public String getStatus() {
        return status;
    }

    public int getCode() {
        return code;
    }
}
