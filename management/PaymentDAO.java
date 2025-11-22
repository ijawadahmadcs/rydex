import java.sql.*;

public class PaymentDAO {

    // Create a payment record
    public int createPayment(int rideId, double amount, String method, String status) {
        String sql = "INSERT INTO Payments (ride_id, amount, payment_method, payment_status) VALUES (?, ?, ?, ?)";
        Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, rideId);
            stmt.setDouble(2, amount);
            stmt.setString(3, method);
            stmt.setString(4, status);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    int newId = rs.getInt(1);
                    // Do not auto-change ride status from payment insertion. Driver must mark completion.
                    return newId;
                }
            } else {
                System.err.println("createPayment: no rows affected when inserting payment for ride " + rideId);
            }

        } catch (SQLException e) {
            // Handle duplicate ride_id (unique constraint) by updating existing payment
            if (e.getErrorCode() == 1062 || "23000".equals(e.getSQLState())) {
                try (PreparedStatement find = conn.prepareStatement("SELECT payment_id FROM Payments WHERE ride_id = ?")) {
                    find.setInt(1, rideId);
                    try (ResultSet found = find.executeQuery()) {
                        if (found.next()) {
                            int existingId = found.getInt(1);
                            try {
                                // Prefer transactionally updating payment+ride by ride_id
                                // Do not auto-change ride status from payment updates.
                                boolean ok = false;
                                if (ok) return existingId;
                                // Fallback: try a simple update if transactional update failed
                                try (PreparedStatement upd = conn.prepareStatement(
                                        "UPDATE Payments SET amount = ?, payment_method = ?, payment_status = ? WHERE payment_id = ?")) {
                                    upd.setDouble(1, amount);
                                    upd.setString(2, method);
                                    upd.setString(3, status);
                                    upd.setInt(4, existingId);
                                    int urows = upd.executeUpdate();
                                    if (urows > 0) return existingId;
                                }
                            } catch (SQLException ex3) {
                                System.err.println("Error updating existing payment transactionally: " + ex3.getMessage());
                            }
                        }
                    }
                } catch (SQLException ex2) {
                    System.err.println("Error updating existing payment after duplicate key: " + ex2.getMessage());
                }
            } else {
                System.err.println("Error creating payment (rideId=" + rideId + "): " + e.getMessage());
            }
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException ignored) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException ignored) {}
            // Do NOT close the shared connection here; DatabaseConfig manages it.
        }
        return -1;
    }

    // Update payment status
    public boolean updatePaymentStatus(int paymentId, String status) {
        // Do NOT change ride status automatically when payment completes.
        // Only update the payment row here; ride status changes should be driven by driver actions.
        String sql = "UPDATE Payments SET payment_status = ? WHERE payment_id = ?";
        try (Connection conn = DatabaseConfig.getNewConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, paymentId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating payment: " + e.getMessage());
            return false;
        }
    }

    // Update payment status and optionally update the associated ride status in one transaction.
    // If rideStatus is null, only the payment row is updated.
    public boolean updatePaymentAndRideStatus(int paymentId, String paymentStatus, String rideStatus) {
        try (Connection conn = DatabaseConfig.getNewConnection()) {
            if (conn == null) {
                System.err.println("Database connection is NULL!");
                return false;
            }
            conn.setAutoCommit(false);

            // Find ride_id for this payment
            int rideId = -1;
            try (PreparedStatement find = conn.prepareStatement("SELECT ride_id FROM Payments WHERE payment_id = ?")) {
                find.setInt(1, paymentId);
                try (ResultSet rs = find.executeQuery()) {
                    if (rs.next()) rideId = rs.getInt("ride_id");
                    else throw new SQLException("Payment not found for id=" + paymentId);
                }
            }

            // Update payment status
            try (PreparedStatement updPay = conn.prepareStatement("UPDATE Payments SET payment_status = ? WHERE payment_id = ?")) {
                updPay.setString(1, paymentStatus);
                updPay.setInt(2, paymentId);
                int u = updPay.executeUpdate();
                if (u == 0) throw new SQLException("Failed to update payment_id=" + paymentId);
            }

            // Optionally update ride status
            if (rideStatus != null && rideId > 0) {
                try (PreparedStatement updRide = conn.prepareStatement("UPDATE Rides SET status = ? WHERE ride_id = ?")) {
                    updRide.setString(1, rideStatus);
                    updRide.setInt(2, rideId);
                    int u2 = updRide.executeUpdate();
                    if (u2 == 0) throw new SQLException("Failed to update ride_id=" + rideId);
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Transaction failed updating payment/ride status: " + e.getMessage());
            return false;
        }
    }

    // Alternative transactional method that targets the payment by ride_id
    // This is useful when the caller already knows the ride_id (safer in create/update flows).
    public boolean updatePaymentAndRideStatusByRideId(int rideId, String paymentStatus, String rideStatus) {
        try (Connection conn = DatabaseConfig.getNewConnection()) {
            if (conn == null) {
                System.err.println("Database connection is NULL!");
                return false;
            }
            conn.setAutoCommit(false);

            // Ensure payment exists for this ride (we will update using ride_id)
            try (PreparedStatement updPay = conn.prepareStatement("UPDATE Payments SET payment_status = ? WHERE ride_id = ?")) {
                updPay.setString(1, paymentStatus);
                updPay.setInt(2, rideId);
                int u = updPay.executeUpdate();
                if (u == 0) throw new SQLException("No payment row updated for ride_id=" + rideId);
            }

            if (rideStatus != null) {
                try (PreparedStatement updRide = conn.prepareStatement("UPDATE Rides SET status = ? WHERE ride_id = ?")) {
                    updRide.setString(1, rideStatus);
                    updRide.setInt(2, rideId);
                    int u2 = updRide.executeUpdate();
                    if (u2 == 0) throw new SQLException("Failed to update ride_id=" + rideId);
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Transaction failed updating payment/ride by ride_id: " + e.getMessage());
            return false;
        }
    }
}
