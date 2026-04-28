import java.util.ArrayList;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class BankAccount {
    private String accountNumber;
    private String accountHolderName;
    private double balance;
    private String pin;
    private ArrayList<Transaction> transactionHistory;
    private LocalDateTime creationDate;
    
    // Constructor
    public BankAccount(String accountNumber, String accountHolderName, double initialDeposit, String pin) {
        this.accountNumber = accountNumber;
        this.accountHolderName = accountHolderName;
        this.balance = initialDeposit;
        this.pin = pin;
        this.transactionHistory = new ArrayList<>();
        this.creationDate = LocalDateTime.now();
        
        if (initialDeposit > 0) {
            transactionHistory.add(new Transaction("DEPOSIT", initialDeposit, "Initial deposit"));
        }
    }
    
    // Getters
    public String getAccountNumber() { return accountNumber; }
    public String getAccountHolderName() { return accountHolderName; }
    public double getBalance() { return balance; }
    public String getPin() { return pin; }
    public LocalDateTime getCreationDate() { return creationDate; }
    
    private String formatCurrency(double amount) {
        return String.format("$%,.2f", amount);
    }
    
    // PIN verification
    public boolean verifyPin(String enteredPin) {
        return this.pin.equals(enteredPin);
    }
    
    // Change PIN
    public boolean changePin(String oldPin, String newPin) {
        if (!verifyPin(oldPin)) {
            System.out.println("❌ Incorrect current PIN!");
            return false;
        }
        if (newPin.length() != 4 || !newPin.matches("\\d+")) {
            System.out.println("❌ PIN must be exactly 4 digits!");
            return false;
        }
        this.pin = newPin;
        System.out.println("✅ PIN changed successfully!");
        return true;
    }
    
    // Deposit
    public boolean deposit(double amount) {
        return deposit(amount, "");
    }
    
    public boolean deposit(double amount, String description) {
        if (amount <= 0) {
            System.out.println("❌ Deposit amount must be positive!");
            return false;
        }
        balance += amount;
        transactionHistory.add(new Transaction("DEPOSIT", amount, description));
        System.out.printf("✅ Successfully deposited $%.2f\n", amount);
        return true;
    }
    
    // Withdraw
    public boolean withdraw(double amount) {
        return withdraw(amount, "");
    }
    
    public boolean withdraw(double amount, String description) {
        if (amount <= 0) {
            System.out.println("❌ Withdrawal amount must be positive!");
            return false;
        }
        if (amount > balance) {
            System.out.println("❌ Insufficient funds!");
            System.out.printf("   Current balance: %s\n", formatCurrency(balance));
            return false;
        }
        balance -= amount;
        transactionHistory.add(new Transaction("WITHDRAWAL", amount, description));
        System.out.printf("✅ Successfully withdrew $%.2f\n", amount);
        return true;
    }
    
    // Calculate interest
    public void calculateInterest(double rate) {
        double interest = balance * (rate / 100);
        balance += interest;
        transactionHistory.add(new Transaction("INTEREST", interest, String.format("%.2f%% annual interest", rate)));
        System.out.println("\n📈 Interest Calculation:");
        System.out.println("   Rate: " + rate + "% per year");
        System.out.println("   Interest Added: " + formatCurrency(interest));
        System.out.println("   New Balance: " + formatCurrency(balance));
    }
    
    // Full transaction history
    public void showTransactionHistory() {
        if (transactionHistory.isEmpty()) {
            System.out.println("No transactions yet.");
            return;
        }
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                    TRANSACTION HISTORY                        ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        for (int i = 0; i < transactionHistory.size(); i++) {
            System.out.printf("║ %3d. %s\n", (i + 1), transactionHistory.get(i));
        }
        System.out.println("╚══════════════════════════════════════════════════════════════╝\n");
    }
    
    // Search by amount range
    public void searchByAmount(double minAmount, double maxAmount) {
        boolean found = false;
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.printf("║     TRANSACTIONS BETWEEN %s - %s%31s\n", 
            formatCurrency(minAmount), formatCurrency(maxAmount), "║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        for (Transaction t : transactionHistory) {
            if (t.getAmount() >= minAmount && t.getAmount() <= maxAmount) {
                System.out.println("║ " + t);
                found = true;
            }
        }
        if (!found) {
            System.out.println("║                    No transactions found in this range                   ║");
        }
        System.out.println("╚══════════════════════════════════════════════════════════════╝\n");
    }
    
    // Filter by type
    public void filterByType(String type) {
        boolean found = false;
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.printf("║                    %s TRANSACTIONS%38s\n", type, "║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        for (Transaction t : transactionHistory) {
            if (t.getType().equals(type)) {
                System.out.println("║ " + t);
                found = true;
            }
        }
        if (!found) {
            System.out.printf("║                    No %s transactions found%35s\n", type, "║");
        }
        System.out.println("╚══════════════════════════════════════════════════════════════╝\n");
    }
    
    // Today's transactions
    public void getTodayTransactions() {
        boolean found = false;
        String todayDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                     TODAY'S TRANSACTIONS                      ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        for (Transaction t : transactionHistory) {
            String transDate = t.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            if (transDate.equals(todayDate)) {
                System.out.println("║ " + t);
                found = true;
            }
        }
        if (!found) {
            System.out.println("║                      No transactions today                        ║");
        }
        System.out.println("╚══════════════════════════════════════════════════════════════╝\n");
    }
    
    // Search by date range
    public void searchByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        boolean found = false;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.printf("║     TRANSACTIONS FROM %s TO %s%26s\n", 
            startDate.format(formatter), endDate.format(formatter), "║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        for (Transaction t : transactionHistory) {
            if (!t.getTimestamp().isBefore(startDate) && !t.getTimestamp().isAfter(endDate)) {
                System.out.println("║ " + t);
                found = true;
            }
        }
        if (!found) {
            System.out.println("║                  No transactions found in this date range                ║");
        }
        System.out.println("╚══════════════════════════════════════════════════════════════╝\n");
    }
    
    // Transaction summary
    public void getTransactionSummary() {
        double totalDeposits = 0, totalWithdrawals = 0, totalInterest = 0;
        int depositCount = 0, withdrawalCount = 0, interestCount = 0;
        
        for (Transaction t : transactionHistory) {
            switch (t.getType()) {
                case "DEPOSIT":
                    totalDeposits += t.getAmount();
                    depositCount++;
                    break;
                case "WITHDRAWAL":
                    totalWithdrawals += t.getAmount();
                    withdrawalCount++;
                    break;
                case "INTEREST":
                    totalInterest += t.getAmount();
                    interestCount++;
                    break;
            }
        }
        
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                    TRANSACTION SUMMARY                        ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.printf("║ Deposits:     %d transaction(s) - Total: %-30s║\n", depositCount, formatCurrency(totalDeposits));
        System.out.printf("║ Withdrawals:  %d transaction(s) - Total: %-30s║\n", withdrawalCount, formatCurrency(totalWithdrawals));
        System.out.printf("║ Interest:     %d transaction(s) - Total: %-30s║\n", interestCount, formatCurrency(totalInterest));
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.printf("║ Net Change:   %-48s║\n", formatCurrency(totalDeposits - totalWithdrawals + totalInterest));
        System.out.printf("║ Current Balance: %-42s║\n", formatCurrency(balance));
        System.out.println("╚══════════════════════════════════════════════════════════════╝\n");
    }
    
    // Monthly statement
    public void getMonthlyStatement(int year, int month) {
        boolean found = false;
        LocalDateTime startDate = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endDate = startDate.plusMonths(1).minusSeconds(1);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.printf("║                 MONTHLY STATEMENT - %s%28s\n", 
            startDate.format(formatter), "║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        
        for (Transaction t : transactionHistory) {
            if (!t.getTimestamp().isBefore(startDate) && !t.getTimestamp().isAfter(endDate)) {
                System.out.println("║ " + t);
                found = true;
            }
        }
        
        if (!found) {
            System.out.println("║                    No transactions this month                     ║");
        }
        System.out.println("╚══════════════════════════════════════════════════════════════╝\n");
    }
    
    // Export to CSV
    public void exportToCSV() {
        String filename = accountNumber + "_transactions.csv";
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("Date,Type,Amount,Description");
            for (Transaction t : transactionHistory) {
                writer.println(t.toCSV());
            }
            System.out.println("✅ Transactions exported to " + filename);
            System.out.println("   You can open this file with Excel");
        } catch (IOException e) {
            System.out.println("❌ Error exporting: " + e.getMessage());
        }
    }
    
    // Delete account (marks as inactive and creates backup)
    public boolean deleteAccount() {
        String backupFile = accountNumber + "_BACKUP_" + System.currentTimeMillis() + ".txt";
        File originalFile = new File(accountNumber + ".txt");
        File backup = new File(backupFile);
        
        if (originalFile.renameTo(backup)) {
            System.out.println("✅ Account " + accountNumber + " has been deleted");
            System.out.println("   Backup saved as: " + backupFile);
            return true;
        }
        return false;
    }
    
    // Save account
    public void saveToFile() {
        String filename = accountNumber + ".txt";
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println(accountNumber);
            writer.println(accountHolderName);
            writer.println(pin);
            writer.println(balance);
            writer.println(creationDate);
            writer.println(transactionHistory.size());
            for (Transaction t : transactionHistory) {
                writer.println(t.getType() + "," + t.getAmount() + "," + t.getTimestamp() + "," + t.getDescription());
            }
            System.out.println("✅ Account saved to " + filename);
        } catch (IOException e) {
            System.out.println("❌ Error saving account: " + e.getMessage());
        }
    }
    
    // Display account info
    public void displayAccountInfo() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║          ACCOUNT INFORMATION          ║");
        System.out.println("╠════════════════════════════════════════╣");
        System.out.println("║ Account Number: " + accountNumber);
        System.out.println("║ Account Holder: " + accountHolderName);
        System.out.println("║ Balance: " + formatCurrency(balance));
        System.out.println("║ Created: " + creationDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        System.out.println("╚════════════════════════════════════════╝\n");
    }
}