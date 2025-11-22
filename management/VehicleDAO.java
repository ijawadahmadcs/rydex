// VehicleDAO.java
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehicleDAO {

    // Add vehicle for driver
    // Add vehicle and associate it to driver by updating Drivers.vehicle_id
    public int addVehicle(int driverId, String model, String plateNumber, int capacity, String color) {
        Connection conn = DatabaseConfig.getConnection();
        try {
            String vehicleSql = "INSERT INTO Vehicles (model, plate_number, capacity, color) VALUES (?, ?, ?, ?)";
            PreparedStatement vehicleStmt = conn.prepareStatement(vehicleSql, Statement.RETURN_GENERATED_KEYS);
            vehicleStmt.setString(1, model);
            vehicleStmt.setString(2, plateNumber);
            vehicleStmt.setInt(3, capacity);
            vehicleStmt.setString(4, color);

            int rowsAffected = vehicleStmt.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet rs = vehicleStmt.getGeneratedKeys();
                if (rs.next()) {
                    int vehicleId = rs.getInt(1);

                    // Update Drivers table with vehicle_id
                    String driverSql = "UPDATE Drivers SET vehicle_id = ? WHERE driver_id = ?";
                    PreparedStatement driverStmt = conn.prepareStatement(driverSql);
                    driverStmt.setInt(1, vehicleId);
                    driverStmt.setInt(2, driverId);
                    driverStmt.executeUpdate();

                    System.out.println("Vehicle added successfully with ID: " + vehicleId);
                    return vehicleId;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding vehicle: " + e.getMessage());
        }
        return -1;
    }

    // Get vehicle by driver ID
    public Vehicle getVehicleByDriverId(int driverId) {
        Connection conn = DatabaseConfig.getConnection();
        try {
            String sql = "SELECT v.* FROM Vehicles v JOIN Drivers d ON v.vehicle_id = d.vehicle_id WHERE d.driver_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, driverId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Vehicle(
                        rs.getInt("vehicle_id"),
                        rs.getString("model"),
                        rs.getString("plate_number"),
                        rs.getInt("capacity"),
                        rs.getString("color")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error getting vehicle: " + e.getMessage());
        }
        return null;
    }

    // Get all drivers with vehicles
    public List<String> getAllDriversWithVehicles() {
        List<String> drivers = new ArrayList<>();
        Connection conn = DatabaseConfig.getConnection();
        try {
            String sql = "SELECT u.user_id, u.name, v.model, v.plate_number, v.capacity, d.total_earnings " +
                    "FROM Users u " +
                    "JOIN Drivers d ON u.user_id = d.driver_id " +
                    "LEFT JOIN Vehicles v ON d.vehicle_id = v.vehicle_id " +
                    "WHERE u.user_type = 'Driver'";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                String driver = String.format(
                        "ID: %d | Name: %s | Vehicle: %s (%s) | Capacity: %d | Earnings: PKR %.2f",
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getString("model") != null ? rs.getString("model") : "No Vehicle",
                        rs.getString("plate_number") != null ? rs.getString("plate_number") : "N/A",
                        rs.getInt("capacity"),
                        rs.getDouble("total_earnings")
                );
                drivers.add(driver);
            }
        } catch (SQLException e) {
            System.err.println("Error getting drivers: " + e.getMessage());
        }
        return drivers;
    }

    // Update vehicle details
    public boolean updateVehicle(int vehicleId, String model, String plateNumber, int capacity, String color) {
        Connection conn = DatabaseConfig.getConnection();
        try {
            String sql = "UPDATE Vehicles SET model = ?, plate_number = ?, capacity = ?, color = ? WHERE vehicle_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, model);
            stmt.setString(2, plateNumber);
            stmt.setInt(3, capacity);
            stmt.setString(4, color);
            stmt.setInt(5, vehicleId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating vehicle: " + e.getMessage());
        }
        return false;
    }
}
