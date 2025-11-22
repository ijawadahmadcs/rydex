public class Vehicle {
    private int vehicleId;
    private String model;
    private String plateNumber;
    private int capacity;
    private String color;

    public Vehicle(int vehicleId, String model, String plateNumber, int capacity, String color) {
        this.vehicleId = vehicleId;
        this.model = model;
        this.plateNumber = plateNumber;
        this.capacity = capacity;
        this.color = color;
    }

    // Getters and Setters
    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int vehicleId) { this.vehicleId = vehicleId; }    

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getPlateNumber() { return plateNumber; }
    public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public void displayVehicleInfo() {
        System.out.printf(
            "\n----- Vehicle Info -----\nID: %d\nModel: %s\nPlate: %s\nCapacity: %d\nColor: %s\n----------------------\n",
            vehicleId, model, plateNumber, capacity, color
        );
    }

    @Override
    public String toString() {
        return String.format("Vehicle{id=%d, model='%s', plate='%s', capacity=%d, color='%s'}",
                             vehicleId, model, plateNumber, capacity, color);
    }
}