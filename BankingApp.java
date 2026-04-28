import java.util.Scanner;
import java.util.HashMap;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BankingApp {
    private static Scanner scanner = new Scanner(System.in);
    private static HashMap<String, BankAccount> accounts = new HashMap<>();
    private static BankAccount currentAdmin = null;
    private static final String ADMIN_PIN = "9999";
    
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║                                                          ║");
        System.out.println("║     🏦  WELCOME TO COMPLETE BANKING SYSTEM  🏦           ║");
        System.out.println("║                                                          ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        
        loadSavedAccounts();
        System.out.println("\n📁 Loaded " + accounts.size() + " account(s)\n");
        
        while (true) {
            displayMainMenu();
            int choice = getIntInput("Enter your choice: ");
            
            switch (choice) {
                case 1:
                    createAccount();
                    break;
                case 2:
                    loginToAccount();
                    break;
                case 3:
                    adminLogin();
                    break;
                case 4:
                    System.out.println("\n✅ Thank you for using Complete Banking System!");
                    System.out.println("   Goodbye!");
                    scanner.close();
                    return;
                default:
                    System.out.println("❌ Invalid choice!");
            }
        }
    }
    
    private static void displayMainMenu() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║              MAIN MENU                 ║");
        System.out.println("╠════════════════════════════════════════╣");
        System.out.println("║ 1. Create New Account                  ║");
        System.out.println("║ 2. Login to Account                    ║");
        System.out.println("║ 3. Admin Login                         ║");
        System.out.println("║ 4. Exit                                ║");
        System.out.println("╚════════════════════════════════════════╝");
    }
    
    private static void loadSavedAccounts() {
        File folder = new File(".");
        File[] files = folder.listFiles();
        
        if (files != null) {
            for (File file : files) {
                String fileName = file.getName();
                if (fileName.matches("\\d+\\.txt") && !fileName.contains("BACKUP")) {
                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        String accNumber = reader.readLine();
                        String holderName = reader.readLine();
                        String pin = reader.readLine();
                        double balance = Double.parseDouble(reader.readLine());
                        reader.readLine(); // skip creation date for loading
                        int numTransactions = Integer.parseInt(reader.readLine());
                        
                        BankAccount account = new BankAccount(accNumber, holderName, balance, pin);
                        accounts.put(accNumber, account);
                    } catch (IOException | NumberFormatException e) {
                        // Skip invalid files
                    }
                }
            }
        }
    }
    
    private static void createAccount() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║         CREATE NEW ACCOUNT             ║");
        System.out.println("╚════════════════════════════════════════╝");
        
        System.out.print("Enter account number: ");
        String accNumber = scanner.nextLine();
        
        if (accounts.containsKey(accNumber)) {
            System.out.println("❌ Account number already exists!");
            return;
        }
        
        System.out.print("Enter account holder name: ");
        String name = scanner.nextLine();
        
        System.out.print("Enter 4-digit PIN: ");
        String pin = scanner.nextLine();
        
        while (pin.length() != 4 || !pin.matches("\\d+")) {
            System.out.println("❌ PIN must be exactly 4 digits!");
            System.out.print("Enter 4-digit PIN: ");
            pin = scanner.nextLine();
        }
        
        double initialDeposit = getDoubleInput("Enter initial deposit amount: $");
        if (initialDeposit < 0) {
            System.out.println("❌ Initial deposit cannot be negative!");
            return;
        }
        
        BankAccount newAccount = new BankAccount(accNumber, name, initialDeposit, pin);
        accounts.put(accNumber, newAccount);
        newAccount.saveToFile();
        
        System.out.println("\n✅ Account created successfully!");
        newAccount.displayAccountInfo();
    }
    
    private static void loginToAccount() {
        System.out.print("\nEnter account number: ");
        String accNumber = scanner.nextLine();
        
        BankAccount account = accounts.get(accNumber);
        if (account == null) {
            System.out.println("❌ Account not found!");
            return;
        }
        
        System.out.print("Enter PIN: ");
        String enteredPin = scanner.nextLine();
        
        if (!account.verifyPin(enteredPin)) {
            System.out.println("❌ Incorrect PIN! Access denied.");
            return;
        }
        
        System.out.println("✅ Login successful!");
        accountMenu(account);
    }
    
    private static void adminLogin() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║           ADMIN LOGIN                   ║");
        System.out.println("╚════════════════════════════════════════╝");
        
        System.out.print("Enter Admin PIN: ");
        String pin = scanner.nextLine();
        
        if (pin.equals(ADMIN_PIN)) {
            System.out.println("✅ Admin access granted!");
            adminMenu();
        } else {
            System.out.println("❌ Invalid Admin PIN!");
        }
    }
    
    private static void adminMenu() {
        while (true) {
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║            ADMIN MENU                   ║");
            System.out.println("╠════════════════════════════════════════╣");
            System.out.println("║ 1. View All Accounts                   ║");
            System.out.println("║ 2. View Account Details                ║");
            System.out.println("║ 3. Delete Account                      ║");
            System.out.println("║ 4. Total Bank Balance                  ║");
            System.out.println("║ 5. Logout                              ║");
            System.out.println("╚════════════════════════════════════════╝");
            
            int choice = getIntInput("Choose an option: ");
            
            switch (choice) {
                case 1:
                    viewAllAccounts();
                    break;
                case 2:
                    viewAccountDetails();
                    break;
                case 3:
                    deleteAccount();
                    break;
                case 4:
                    totalBankBalance();
                    break;
                case 5:
                    System.out.println("🔒 Logging out from Admin...");
                    return;
                default:
                    System.out.println("❌ Invalid option!");
            }
        }
    }
    
    private static void viewAllAccounts() {
        if (accounts.isEmpty()) {
            System.out.println("No accounts in the system.");
            return;
        }
        
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                     ALL ACCOUNTS                              ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        for (BankAccount acc : accounts.values()) {
            System.out.printf("║ %-10s | %-20s | $%,12.2f%27s\n", 
                acc.getAccountNumber(), 
                acc.getAccountHolderName(), 
                acc.getBalance(), "║");
        }
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
    }
    
    private static void viewAccountDetails() {
        System.out.print("Enter account number: ");
        String accNumber = scanner.nextLine();
        BankAccount account = accounts.get(accNumber);
        
        if (account == null) {
            System.out.println("❌ Account not found!");
            return;
        }
        
        account.displayAccountInfo();
    }
    
    private static void deleteAccount() {
        System.out.print("Enter account number to delete: ");
        String accNumber = scanner.nextLine();
        BankAccount account = accounts.get(accNumber);
        
        if (account == null) {
            System.out.println("❌ Account not found!");
            return;
        }
        
        System.out.print("Are you sure? Type 'DELETE' to confirm: ");
        String confirm = scanner.nextLine();
        
        if (confirm.equals("DELETE")) {
            if (account.deleteAccount()) {
                accounts.remove(accNumber);
            }
        } else {
            System.out.println("❌ Deletion cancelled.");
        }
    }
    
    private static void totalBankBalance() {
        double total = 0;
        for (BankAccount acc : accounts.values()) {
            total += acc.getBalance();
        }
        System.out.printf("\n💰 Total Bank Balance: $%,.2f\n", total);
        System.out.printf("   Total Accounts: %d\n", accounts.size());
    }
    
    private static void accountMenu(BankAccount account) {
        while (true) {
            displayAccountMenu();
            int choice = getIntInput("Choose an option: ");
            
            switch (choice) {
                case 1:
                    double depositAmount = getDoubleInput("Enter amount to deposit: $");
                    System.out.print("Enter description (optional): ");
                    String depositDesc = scanner.nextLine();
                    if (depositDesc.isEmpty()) {
                        account.deposit(depositAmount);
                    } else {
                        account.deposit(depositAmount, depositDesc);
                    }
                    account.saveToFile();
                    break;
                case 2:
                    double withdrawAmount = getDoubleInput("Enter amount to withdraw: $");
                    System.out.print("Enter description (optional): ");
                    String withdrawDesc = scanner.nextLine();
                    if (withdrawDesc.isEmpty()) {
                        account.withdraw(withdrawAmount);
                    } else {
                        account.withdraw(withdrawAmount, withdrawDesc);
                    }
                    account.saveToFile();
                    break;
                case 3:
                    account.displayAccountInfo();
                    break;
                case 4:
                    account.showTransactionHistory();
                    break;
                case 5:
                    account.saveToFile();
                    break;
                case 6:
                    double rate = getDoubleInput("Enter annual interest rate (%): ");
                    account.calculateInterest(rate);
                    account.saveToFile();
                    break;
                case 7:
                    transferMoney(account);
                    break;
                case 8:
                    changePin(account);
                    break;
                case 9:
                    account.getTransactionSummary();
                    break;
                case 10:
                    searchByAmount(account);
                    break;
                case 11:
                    filterByType(account);
                    break;
                case 12:
                    account.getTodayTransactions();
                    break;
                case 13:
                    searchByDateRange(account);
                    break;
                case 14:
                    monthlyStatement(account);
                    break;
                case 15:
                    account.exportToCSV();
                    break;
                case 16:
                    System.out.println("🔒 Logging out...");
                    return;
                default:
                    System.out.println("❌ Invalid option!");
            }
        }
    }
    
    private static void displayAccountMenu() {
        System.out.println("\n┌─────────────────────────────────────────────────┐");
        System.out.println("│                 ACCOUNT MENU                     │");
        System.out.println("├─────────────────────────────────────────────────┤");
        System.out.println("│ 1. Deposit                                      │");
        System.out.println("│ 2. Withdraw                                     │");
        System.out.println("│ 3. Check Balance                                │");
        System.out.println("│ 4. Transaction History                          │");
        System.out.println("│ 5. Save Account                                 │");
        System.out.println("│ 6. Calculate Interest                           │");
        System.out.println("│ 7. Transfer Money                               │");
        System.out.println("│ 8. Change PIN                                   │");
        System.out.println("│ 9. Transaction Summary                          │");
        System.out.println("│10. Search by Amount                             │");
        System.out.println("│11. Filter by Type                               │");
        System.out.println("│12. Today's Transactions                         │");
        System.out.println("│13. Search by Date Range                         │");
        System.out.println("│14. Monthly Statement                            │");
        System.out.println("│15. Export to CSV (Excel)                        │");
        System.out.println("│16. Logout                                       │");
        System.out.println("└─────────────────────────────────────────────────┘");
    }
    
    private static void transferMoney(BankAccount fromAccount) {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║            TRANSFER MONEY               ║");
        System.out.println("╚════════════════════════════════════════╝");
        
        System.out.print("Enter recipient account number: ");
        String toAccountNumber = scanner.nextLine();
        
        BankAccount toAccount = accounts.get(toAccountNumber);
        if (toAccount == null) {
            System.out.println("❌ Recipient account not found!");
            return;
        }
        
        if (toAccountNumber.equals(fromAccount.getAccountNumber())) {
            System.out.println("❌ Cannot transfer to the same account!");
            return;
        }
        
        double amount = getDoubleInput("Enter amount to transfer: $");
        System.out.print("Enter description (optional): ");
        String desc = scanner.nextLine();
        
        System.out.print("Enter your PIN to confirm: ");
        String confirmPin = scanner.nextLine();
        
        if (!fromAccount.verifyPin(confirmPin)) {
            System.out.println("❌ Incorrect PIN! Transfer cancelled.");
            return;
        }
        
        if (fromAccount.withdraw(amount, "Transfer to " + toAccountNumber + (desc.isEmpty() ? "" : " - " + desc))) {
            toAccount.deposit(amount, "Transfer from " + fromAccount.getAccountNumber() + (desc.isEmpty() ? "" : " - " + desc));
            System.out.printf("✅ Successfully transferred $%.2f to account %s\n", amount, toAccountNumber);
            fromAccount.saveToFile();
            toAccount.saveToFile();
        }
    }
    
    private static void changePin(BankAccount account) {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║              CHANGE PIN                 ║");
        System.out.println("╚════════════════════════════════════════╝");
        
        System.out.print("Enter current PIN: ");
        String oldPin = scanner.nextLine();
        System.out.print("Enter new 4-digit PIN: ");
        String newPin = scanner.nextLine();
        
        account.changePin(oldPin, newPin);
        if (account.verifyPin(newPin)) {
            account.saveToFile();
        }
    }
    
    private static void searchByAmount(BankAccount account) {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║           SEARCH BY AMOUNT              ║");
        System.out.println("╚════════════════════════════════════════╝");
        
        double minAmount = getDoubleInput("Enter minimum amount: $");
        double maxAmount = getDoubleInput("Enter maximum amount: $");
        
        if (minAmount > maxAmount) {
            System.out.println("❌ Minimum cannot be greater than maximum!");
            return;
        }
        account.searchByAmount(minAmount, maxAmount);
    }
    
    private static void filterByType(BankAccount account) {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║            FILTER BY TYPE               ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.println("1. DEPOSITS");
        System.out.println("2. WITHDRAWALS");
        System.out.println("3. INTEREST");
        
        int typeChoice = getIntInput("Choose: ");
        String type = "";
        switch (typeChoice) {
            case 1: type = "DEPOSIT"; break;
            case 2: type = "WITHDRAWAL"; break;
            case 3: type = "INTEREST"; break;
            default: System.out.println("❌ Invalid!"); return;
        }
        account.filterByType(type);
    }
    
    private static void searchByDateRange(BankAccount account) {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║          SEARCH BY DATE RANGE           ║");
        System.out.println("╚════════════════════════════════════════╝");
        
        System.out.println("Enter START date:");
        int startYear = getIntInput("  Year (YYYY): ");
        int startMonth = getIntInput("  Month (1-12): ");
        int startDay = getIntInput("  Day (1-31): ");
        
        System.out.println("Enter END date:");
        int endYear = getIntInput("  Year (YYYY): ");
        int endMonth = getIntInput("  Month (1-12): ");
        int endDay = getIntInput("  Day (1-31): ");
        
        LocalDateTime startDate = LocalDateTime.of(startYear, startMonth, startDay, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(endYear, endMonth, endDay, 23, 59);
        
        account.searchByDateRange(startDate, endDate);
    }
    
    private static void monthlyStatement(BankAccount account) {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║           MONTHLY STATEMENT             ║");
        System.out.println("╚════════════════════════════════════════╝");
        
        int year = getIntInput("Enter year (YYYY): ");
        int month = getIntInput("Enter month (1-12): ");
        
        account.getMonthlyStatement(year, month);
    }
    
    private static int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("❌ Please enter a valid number!");
            }
        }
    }
    
    private static double getDoubleInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Double.parseDouble(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("❌ Please enter a valid amount!");
            }
        }
    }
}