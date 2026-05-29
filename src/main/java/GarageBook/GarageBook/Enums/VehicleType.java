package GarageBook.GarageBook.Enums;

public enum VehicleType {
    CAR("CAR", 1),
    BIKE("BIKE", 2),
    TRUCK("TRUCK", 3),
    SUV("SUV", 4),
    SEDAN("SEDAN", 5);

    String vehicleType;
    int code;

    VehicleType(String vehicleType, int code) {
        this.vehicleType = vehicleType;
        this.code = code;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public int getCode() {
        return code;
    }
}
