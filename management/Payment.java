// Payment.java - Interface demonstrating Polymorphism
public interface Payment {
    boolean processPayment(double amount); // process the payment
    String getPaymentMethod();             // return method name
    void displayPaymentDetails();          // show payment info
}

// CashPayment.java
class CashPayment implements Payment {

    private int paymentId;
    private int rideId;
    private double amount;
    private String status;

    public CashPayment(int paymentId, int rideId, double amount) {
        this.paymentId = paymentId;
        this.rideId = rideId;
        this.amount = amount;
        this.status = "Pending";
    }

    @Override
    public boolean processPayment(double amount) {
        this.status = "Completed";
        System.out.println("Cash payment of PKR " + String.format("%.2f", amount) + " received.");
        return true;
    }

    @Override
    public String getPaymentMethod() {
        return "Cash";
    }

    @Override
    public void displayPaymentDetails() {
        System.out.println("Payment Method: Cash");
        System.out.println("Amount: PKR " + String.format("%.2f", amount));
        System.out.println("Status: " + status);
    }
}

// CardPayment.java
class CardPayment implements Payment {

    private int paymentId;
    private int rideId;
    private double amount;
    private String status;
    private String cardNumber;

    public CardPayment(int paymentId, int rideId, double amount, String cardNumber) {
        this.paymentId = paymentId;
        this.rideId = rideId;
        this.amount = amount;
        this.cardNumber = cardNumber;
        this.status = "Pending";
    }

    @Override
    public boolean processPayment(double amount) {
        System.out.println("Processing card payment...");
        this.status = "Completed";
        System.out.println("Card payment of PKR " + String.format("%.2f", amount) + " successful.");
        return true;
    }

    @Override
    public String getPaymentMethod() {
        return "Card";
    }

    @Override
    public void displayPaymentDetails() {
        System.out.println("Payment Method: Card");
        System.out.println("Card: **** **** **** **** " + cardNumber.substring(cardNumber.length() - 4));
        System.out.println("Amount: PKR " + String.format("%.2f", amount));
        System.out.println("Status: " + status);
    }
}

// WalletPayment.java
class WalletPayment implements Payment {

    private int paymentId;
    private int rideId;
    private double amount;
    private String status;
    private Rider rider; // associated rider

    public WalletPayment(int paymentId, int rideId, double amount, Rider rider) {
        this.paymentId = paymentId;
        this.rideId = rideId;
        this.amount = amount;
        this.rider = rider;
        this.status = "Pending";
    }

    @Override
    public boolean processPayment(double amount) {
        if (rider.getBalance() >= amount) {
            rider.deductBalance(amount);
            this.status = "Completed";
            System.out.println("Wallet payment of PKR " + String.format("%.2f", amount) + " successful.");
            System.out.println("Remaining balance: PKR " + String.format("%.2f", rider.getBalance()));
            return true;
        } else {
            this.status = "Failed";
            System.out.println("Insufficient wallet balance!");
            return false;
        }
    }

    @Override
    public String getPaymentMethod() {
        return "Wallet";
    }

    @Override
    public void displayPaymentDetails() {
        System.out.println("Payment Method: Wallet");
        System.out.println("Amount: PKR " + String.format("%.2f", amount));
        System.out.println("Status: " + status);
        System.out.println("Current Balance: PKR " + String.format("%.2f", rider.getBalance()));
    }
}
