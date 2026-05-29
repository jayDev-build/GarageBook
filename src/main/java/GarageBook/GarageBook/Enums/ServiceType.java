package GarageBook.GarageBook.Enums;

public enum ServiceType {
    GENERAL_SERVICE("GENERAL SERVICE", 1),
    WASH("WASH", 2),
    REPAIR("REPAIR", 3),
    OIL_CHANGE("OIL CHANGE", 4);

    String serviceType;
    int code;

    ServiceType(String serviceType, int code) {
        this.serviceType = serviceType;
        this.code = code;
    }

    public String getServiceType() {
        return serviceType;
    }

    public int getCode() {
        return code;
    }
}
