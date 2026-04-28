import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction {
    private String type;      // "DEPOSIT", "WITHDRAWAL", or "INTEREST"
    private double amount;
    private LocalDateTime timestamp;
    private String description;  // Optional description for transaction
    
    // Constructor
    public Transaction(String type, double amount) {
        this.type = type;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
        this.description = "";
    }
    
    // Constructor with description
    public Transaction(String type, double amount, String description) {
        this.type = type;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
        this.description = description;
    }
    
    // Getter methods
    public String getType() { 
        return type; 
    }
    
    public double getAmount() { 
        return amount; 
    }
    
    public LocalDateTime getTimestamp() { 
        return timestamp; 
    }
    
    public String getDescription() {
        return description;
    }
    
    // Convert transaction to readable string
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (description.isEmpty()) {
            return String.format("%s - %s: $%.2f", 
                timestamp.format(formatter), type, amount);
        } else {
            return String.format("%s - %s: $%.2f (%s)", 
                timestamp.format(formatter), type, amount, description);
        }
    }
    
    // For CSV export
    public String toCSV() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return String.format("%s,%s,%.2f,%s", 
            timestamp.format(formatter), type, amount, description);
    }
}