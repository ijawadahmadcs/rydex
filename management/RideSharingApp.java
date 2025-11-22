import java.util.*;

public class RideSharingApp {

    private static Scanner scanner = new Scanner(System.in);
    private static UserDAO userDAO = new UserDAO();
    private static RideDAO rideDAO = new RideDAO();
    private static RouteDAO routeDAO = new RouteDAO();
    private static VehicleDAO vehicleDAO = new VehicleDAO();
    private static PaymentDAO paymentDAO = new PaymentDAO();
    private static FeedbackDAO feedbackDAO = new FeedbackDAO();
    private static DriverShiftDAO shiftDAO = new DriverShiftDAO();
    private static RideAssistantDAO assistantDAO = new RideAssistantDAO();

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════╗");
        System.out.println("║   CAMPUS RIDE-SHARING MANAGEMENT SYSTEM    ║");
        System.out.println("╚════════════════════════════════════════════╝");

        DatabaseConfig.getConnection(); // connect database

        boolean running = true;
        while (running) {
            System.out.println("\n========== MAIN MENU ==========");
            System.out.println("1. Register");
            System.out.println("2. Login as Driver");
            System.out.println("3. Login as Rider");
            System.out.println("4. Exit");
            System.out.println("===============================");
            System.out.print("Choose an option: ");

            int choice = getIntInput();

            switch (choice) {
                case 1 -> registerMenu();
                case 2 -> driverLogin();
                case 3 -> riderLogin();
                case 4 -> {
                    System.out.println("Thank you for using Campus Ride-Sharing System!");
                    DatabaseConfig.closeConnection();
                    running = false;
                }
                default -> System.out.println("Invalid option!");
            }
        }
        scanner.close();
    }

    // ===================== REGISTRATION =====================
    private static void registerMenu() {
        System.out.println("\n--- REGISTRATION ---");
        System.out.println("1. Register as Driver");
        System.out.println("2. Register as Rider");
        System.out.print("Choose: ");

        int choice = getIntInput();

        System.out.print("Enter Name: ");
        String name = getNonEmptyInput("Name");

        System.out.print("Enter Email: ");
        String email = getNonEmptyInput("Email");

        System.out.print("Enter Password: ");
        String password = getNonEmptyInput("Password");

        if (choice == 1) {
            System.out.print("Enter License Number: ");
            String license = getNonEmptyInput("License Number");
            userDAO.registerDriver(name, email, password, license);
        } else if (choice == 2) {
            userDAO.registerRider(name, email, password);
        } else {
            System.out.println("Invalid choice!");
        }
    }

    // ===================== DRIVER =====================
    private static void driverLogin() {
        System.out.println("\n--- DRIVER LOGIN ---");
        System.out.print("Email: ");
        String email = getNonEmptyInput("Email");
        System.out.print("Password: ");
        String password = getNonEmptyInput("Password");

        Driver driver = userDAO.loginDriver(email, password);

        if (driver != null) {
            System.out.println("\n✓ Login successful! Welcome, " + driver.getName());
            Vehicle vehicle = vehicleDAO.getVehicleByDriverId(driver.getUserId());
            if (vehicle != null) driver.setVehicle(vehicle);
            driverMenu(driver);
        } else {
            System.out.println("✗ Invalid credentials!");
        }
    }

    private static void driverMenu(Driver driver) {
        boolean running = true;
        while (running) {
            System.out.println("\n========== DRIVER MENU ==========");
            System.out.println("1. View Profile");
            System.out.println("2. Add/Update Vehicle");
            System.out.println("3. View My Rides");
            System.out.println("4. Start Ride (mark In Progress)");
            System.out.println("5. Complete Ride (mark Completed)");
            System.out.println("6. View Available Drivers");
            System.out.println("7. View Shifts");
            System.out.println("8. Add Shift");
            System.out.println("9. Logout");
            System.out.println("=================================");
            System.out.print("Choose: ");

            int choice = getIntInput();

            switch (choice) {
                case 1 -> driver.displayProfile();
                case 2 -> addOrUpdateVehicle(driver);
                case 3 -> viewDriverRides(driver);
                case 4 -> startRideAction(driver);
                case 5 -> completeRideAction(driver);
                case 6 -> viewAllDrivers();
                case 7 -> viewDriverShifts(driver);
                case 8 -> addDriverShift(driver);
                case 9 -> running = false;
                default -> System.out.println("Invalid option!");
            }
        }
    }

    private static void startRideAction(Driver driver) {
        System.out.println("\n--- START RIDE ---");
        System.out.print("Enter Ride ID to start: ");
        int rideId = getIntInput();
        if (rideId <= 0) {
            System.out.println("Invalid Ride ID");
            return;
        }
        boolean ok = rideDAO.startRideTransaction(rideId, driver.getUserId());
        if (ok) System.out.println("Ride marked In Progress.");
        else System.out.println("Failed to mark ride as In Progress. Ensure you're the assigned driver and the ride isn't already started/completed.");
    }

    private static void completeRideAction(Driver driver) {
        System.out.println("\n--- COMPLETE RIDE ---");
        System.out.print("Enter Ride ID to complete: ");
        int rideId = getIntInput();
        if (rideId <= 0) {
            System.out.println("Invalid Ride ID");
            return;
        }
        double fare = rideDAO.getFareByRideId(rideId);
        if (fare < 0) {
            System.out.println("Could not determine fare for ride.");
            return;
        }
        boolean ok = rideDAO.completeRideTransaction(rideId, driver.getUserId(), fare);
        System.out.println(ok ? "Ride completed and earnings updated." : "Failed to complete ride.");
        // If successful, update in-memory driver earnings
        if (ok) driver.addEarnings(fare);
    }

    private static void viewDriverShifts(Driver driver) {
        System.out.println("\n--- YOUR SHIFTS ---");
        var shifts = shiftDAO.getShiftsByDriver(driver.getUserId());
        if (shifts.isEmpty()) System.out.println("No shifts set.");
        else shifts.forEach(System.out::println);
    }

    private static void addDriverShift(Driver driver) {
        System.out.println("\n--- ADD SHIFT ---");
        try {
            System.out.print("Shift Date (YYYY-MM-DD): ");
            String dateStr = getNonEmptyInput("Shift Date");
            System.out.print("Start Time (HH:MM:SS): ");
            String startStr = getNonEmptyInput("Start Time");
            System.out.print("End Time (HH:MM:SS): ");
            String endStr = getNonEmptyInput("End Time");

            java.sql.Date date = java.sql.Date.valueOf(dateStr);
            java.sql.Time start = java.sql.Time.valueOf(startStr);
            java.sql.Time end = java.sql.Time.valueOf(endStr);

            boolean ok = shiftDAO.addShift(driver.getUserId(), date, start, end);
            System.out.println(ok ? "Shift added." : "Failed to add shift.");
        } catch (Exception e) {
            System.out.println("Invalid date/time format: " + e.getMessage());
        }
    }

    // ===================== RIDER =====================
    private static void riderLogin() {
        System.out.println("\n--- RIDER LOGIN ---");
        System.out.print("Email: ");
        String email = getNonEmptyInput("Email");
        System.out.print("Password: ");
        String password = getNonEmptyInput("Password");

        Rider rider = userDAO.loginRider(email, password);

        if (rider != null) {
            System.out.println("\n✓ Login successful! Welcome, " + rider.getName());
            riderMenu(rider);
        } else {
            System.out.println("✗ Invalid credentials!");
        }
    }

    private static void riderMenu(Rider rider) {
        boolean running = true;
        while (running) {
            System.out.println("\n========== RIDER MENU ==========");
            System.out.println("1. View Profile");
            System.out.println("2. Add Money to Wallet");
            System.out.println("3. Book a Ride");
            System.out.println("4. View My Rides");
            System.out.println("5. View All Routes");
            System.out.println("6. Submit Feedback");
            System.out.println("7. Logout");
            System.out.println("================================");
            System.out.print("Choose: ");

            int choice = getIntInput();

            switch (choice) {
                case 1 -> rider.displayProfile();
                case 2 -> addMoneyToWallet(rider);
                case 3 -> bookRide(rider);
                case 4 -> viewRiderRides(rider);
                case 5 -> viewAllRoutes();
                case 6 -> submitFeedback(rider);
                case 7 -> running = false;
                default -> System.out.println("Invalid option!");
            }
        }
    }

    // ===================== VEHICLES =====================
    private static void addOrUpdateVehicle(Driver driver) {
        System.out.println("\n--- ADD/UPDATE VEHICLE ---");
        System.out.print("Model: ");
        String model = getNonEmptyInput("Model");
        System.out.print("Plate Number: ");
        String plateNumber = getNonEmptyInput("Plate Number");
        System.out.print("Capacity: ");
        int capacity = getIntInput();
        System.out.print("Color: ");
        String color = getNonEmptyInput("Color");

        int vehicleId = vehicleDAO.addVehicle(driver.getUserId(), model, plateNumber, capacity, color);
        if (vehicleId > 0) {
            Vehicle vehicle = new Vehicle(vehicleId, model, plateNumber, capacity, color);
            driver.setVehicle(vehicle);
        }
    }

    // ===================== WALLET =====================
    private static void addMoneyToWallet(Rider rider) {
        System.out.print("Enter amount to add (PKR): ");
        double amount = getDoubleInput();
        if (amount > 0) {
            rider.addBalance(amount);
            userDAO.updateRiderBalance(rider.getUserId(), rider.getBalance());
            System.out.println("✓ Added successfully. New balance: PKR " + rider.getBalance());
        } else {
            System.out.println("Invalid amount!");
        }
    }

    // ===================== BOOK RIDE =====================
    private static void bookRide(Rider rider) {
        System.out.println("\n--- BOOK A RIDE ---");

        List<String> drivers = vehicleDAO.getAllDriversWithVehicles();
        if (drivers.isEmpty()) {
            System.out.println("No drivers available!");
            return;
        }
        System.out.println("\nAvailable Drivers:");
        for (String d : drivers) System.out.println(d);

        System.out.print("\nEnter Driver ID: ");
        int driverId = getIntInput();

        List<Route> routes = routeDAO.getAllRoutes();
        if (routes.isEmpty()) {
            routeDAO.addRoute("Main Campus", "Engineering Block", 2.5);
            routeDAO.addRoute("Library", "Student Center", 1.8);
            routeDAO.addRoute("Hostel A", "Science Building", 3.2);
            routes = routeDAO.getAllRoutes();
        }
        System.out.println("\nAvailable Routes:");
        for (int i = 0; i < routes.size(); i++) System.out.println((i + 1) + ". " + routes.get(i));

        System.out.print("\nSelect Route (1-" + routes.size() + "): ");
        int rIndex = getIntInput() - 1;
        if (rIndex < 0 || rIndex >= routes.size()) {
            System.out.println("Invalid route!");
            return;
        }
        Route selected = routes.get(rIndex);
        double fare = calculateFare(selected.getDistanceKm());

        System.out.println("\nRoute: " + selected);
        System.out.println("Fare: PKR " + fare);

        // Payment selection before confirming booking
        String method = null;
        boolean walletOk = false;
        String cardNumber = null;
        while (method == null) {
            System.out.println("\nSelect Payment Method:");
            System.out.println("1. Cash");
            System.out.println("2. Card");
            System.out.println("3. Wallet");
            System.out.print("Choose: ");
            int pm = getIntInput();
            switch (pm) {
                case 1 -> method = "Cash";
                case 2 -> {
                    method = "Card";
                    System.out.print("Enter Card Number (demo): ");
                    cardNumber = getNonEmptyInput("Card Number");
                }
                case 3 -> {
                    method = "Wallet";
                    if (rider.getBalance() >= fare) {
                        walletOk = true;
                    } else {
                        System.out.println("Insufficient wallet balance. Add money or choose another method.");
                        System.out.print("Add money now? (y/n): ");
                        String add = getNonEmptyInput("Add money choice");
                        if (add.equalsIgnoreCase("y")) {
                            System.out.print("Enter amount to add (PKR): ");
                            double amt = getDoubleInput();
                            if (amt > 0) {
                                rider.addBalance(amt);
                                userDAO.updateRiderBalance(rider.getUserId(), rider.getBalance());
                                if (rider.getBalance() >= fare) walletOk = true;
                            } else {
                                System.out.println("Invalid amount!");
                            }
                        }
                        if (!walletOk) method = null; // re-prompt
                    }
                }
                default -> System.out.println("Invalid option!");
            }
        }

        System.out.print("\nConfirm booking and pay with " + method + "? (y/n): ");
        String conf2 = getNonEmptyInput("Confirmation");
        if (!conf2.equalsIgnoreCase("y")) {
            System.out.println("Booking cancelled.");
            return;
        }

        int rideId = rideDAO.createRide(rider.getUserId(), driverId, selected.getRouteId(), fare);
        if (rideId <= 0) {
            System.out.println("Failed to create ride.");
            return;
        }

        boolean paymentSuccess = false;
        if (method.equals("Wallet")) {
            if (walletOk && rider.deductBalance(fare)) {
                userDAO.updateRiderBalance(rider.getUserId(), rider.getBalance());
                paymentSuccess = true;
            } else {
                paymentSuccess = false;
            }
        } else if (method.equals("Card")) {
            // Simulate card processing
            System.out.println("Processing card ending with: " + (cardNumber != null && cardNumber.length() >= 4 ? cardNumber.substring(cardNumber.length()-4) : "XXXX"));
            paymentSuccess = true;
        } else {
            // Cash assumed to be paid on pickup
            paymentSuccess = true;
        }

        String payStatus = paymentSuccess ? "Completed" : "Failed";
        int payId = paymentDAO.createPayment(rideId, fare, method, payStatus);
        if (payId > 0) System.out.println("Payment recorded (ID: " + payId + ") Status: " + payStatus);
        else System.out.println("Failed to record payment.");

        System.out.println("✓ Ride booked successfully! ID: " + rideId);

        // Offer to add assistants (companions)
        System.out.print("Add an assistant/companion? (y/n): ");
        String ans = getNonEmptyInput("Assistant choice");
        if (ans.equalsIgnoreCase("y")) {
            System.out.print("Assistant name: ");
            String an = getNonEmptyInput("Assistant name");
            boolean added = assistantDAO.addAssistant(rideId, rider.getUserId(), an);
            System.out.println(added ? "Assistant added." : "Failed to add assistant.");
        }
    }

    private static double calculateFare(double km) {
        return 100 + km * 50; // PKR calculation
    }

    // ===================== VIEW DETAILS =====================
    private static void viewRiderRides(Rider rider) {
        List<String> rides = rideDAO.getRidesByRider(rider.getUserId());
        System.out.println("\n--- MY RIDES ---");
        if (rides.isEmpty()) System.out.println("No rides yet!");
        else rides.forEach(System.out::println);
    }

    private static void viewDriverRides(Driver driver) {
        List<String> rides = rideDAO.getRidesByDriver(driver.getUserId());
        System.out.println("\n--- MY RIDES ---");
        if (rides.isEmpty()) System.out.println("No rides yet!");
        else rides.forEach(System.out::println);
    }

    private static void viewAllRoutes() {
        List<Route> routes = routeDAO.getAllRoutes();
        System.out.println("\n--- ALL ROUTES ---");
        if (routes.isEmpty()) System.out.println("No routes!");
        else routes.forEach(Route::displayRoute);
    }

    private static void viewAllDrivers() {
        List<String> drivers = vehicleDAO.getAllDriversWithVehicles();
        System.out.println("\n--- ALL DRIVERS ---");
        if (drivers.isEmpty()) System.out.println("No drivers!");
        else drivers.forEach(System.out::println);
    }

    // ===================== PAYMENT DEMO =====================
    private static void paymentDemo(Rider rider) {
        System.out.println("\n========== PAYMENT DEMO ==========");
        System.out.print("Enter amount to pay (PKR): ");
        double amount = getDoubleInput();

        System.out.print("(Optional) Enter Ride ID to link payment (or 0): ");
        int rideId = getIntInput();

        System.out.println("\n1. Cash");
        System.out.println("2. Card");
        System.out.println("3. Wallet");
        System.out.print("Choose: ");
        int choice = getIntInput();

        Payment payment;
        boolean success = false;
        String method = "Unknown";
        switch (choice) {
            case 1 -> {
                payment = new CashPayment(0, rideId, amount);
                success = payment.processPayment(amount);
                method = "Cash";
            }
            case 2 -> {
                System.out.print("Card Number: ");
                String card = getNonEmptyInput("Card Number");
                payment = new CardPayment(0, rideId, amount, card);
                success = payment.processPayment(amount);
                method = "Card";
            }
            case 3 -> {
                payment = new WalletPayment(0, rideId, amount, rider);
                success = payment.processPayment(amount);
                method = "Wallet";
                if (success) userDAO.updateRiderBalance(rider.getUserId(), rider.getBalance());
            }
            default -> {
                System.out.println("Invalid method!");
                return;
            }
        }

        if (rideId > 0) {
            String status = success ? "Completed" : "Failed";
            int payId = paymentDAO.createPayment(rideId, amount, method, status);
            if (payId > 0) System.out.println("Payment recorded with ID: " + payId);
            else System.out.println("Failed to persist payment record.");
        }
    }

    private static void submitFeedback(Rider rider) {
        System.out.println("\n--- SUBMIT FEEDBACK ---");
        viewRiderRides(rider);
        System.out.print("Enter Ride ID to submit feedback for: ");
        int rideId = getIntInput();
        if (rideId <= 0) {
            System.out.println("Invalid ride id.");
            return;
        }
        System.out.print("Rating (1-5): ");
        int rating = getIntInput();
        if (rating < 1 || rating > 5) {
            System.out.println("Rating must be between 1 and 5!");
            return;
        }
        System.out.print("Comments: ");
        String comments = getNonEmptyInput("Comments");

        int fbId = feedbackDAO.createFeedback(rideId, rating, comments);
        if (fbId > 0) System.out.println("Feedback submitted. ID: " + fbId);
        else System.out.println("Failed to submit feedback (maybe one feedback per ride only).");
    }

    // ===================== UTILITY METHODS =====================
    private static int getIntInput() {
        while (true) {
            try {
                String input = scanner.nextLine().trim();
                if (!input.isEmpty()) {
                    return Integer.parseInt(input);
                }
                System.out.print("Please enter a valid number: ");
            } catch (Exception e) {
                System.out.print("Invalid number! Please enter a valid integer: ");
            }
        }
    }

    private static double getDoubleInput() {
        while (true) {
            try {
                String input = scanner.nextLine().trim();
                if (!input.isEmpty()) {
                    return Double.parseDouble(input);
                }
                System.out.print("Please enter a valid number: ");
            } catch (Exception e) {
                System.out.print("Invalid number! Please enter a valid number: ");
            }
        }
    }

    private static String getNonEmptyInput(String fieldName) {
        while (true) {
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.print(fieldName + " cannot be empty. Please enter " + fieldName.toLowerCase() + ": ");
        }
    }
}