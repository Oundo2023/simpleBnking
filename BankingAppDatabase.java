import java.util.Scanner;

public class BankingAppDatabase {
    private static Scanner scanner = new Scanner(System.in);
    private static DatabaseConnection db;
    private static String currentAccount = null;
    
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║     🏦  PROFESSIONAL BANKING SYSTEM (XAMPP)  🏦          ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        
        // Initialize database connection
        db = new DatabaseConnection();
        
        while (true) {
            displayMainMenu();
            int choice = getIntInput("Enter your choice: ");
            
            switch (choice) {
                case 1:
                    createAccount();
                    break;
                case 2:
                    login();
                    break;
                case 3:
                    System.out.println("\n✅ Thank you for using Professional Banking System!");
                    db.close();
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
        System.out.println("║ 3. Exit                                ║");
        System.out.println("╚════════════════════════════════════════╝");
    }
    
    private static void createAccount() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║         CREATE NEW ACCOUNT             ║");
        System.out.println("╚════════════════════════════════════════╝");
        
        System.out.print("Enter account number: ");
        String accNumber = scanner.nextLine();
        
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
        
        if (db.createAccount(accNumber, name, pin, initialDeposit)) {
            System.out.println("\n✅ Account created successfully!");
            displayAccountInfo(accNumber);
        } else {
            System.out.println("❌ Account number already exists!");
        }
    }
    
    private static void login() {
        System.out.print("\nEnter account number: ");
        String accNumber = scanner.nextLine();
        
        System.out.print("Enter PIN: ");
        String pin = scanner.nextLine();
        
        if (db.verifyLogin(accNumber, pin)) {
            currentAccount = accNumber;
            System.out.println("✅ Login successful!");
            accountMenu();
        } else {
            System.out.println("❌ Invalid account number or PIN!");
        }
    }
    
    private static void accountMenu() {
        while (true) {
            displayAccountMenu();
            int choice = getIntInput("Choose an option: ");
            
            switch (choice) {
                case 1:
                    deposit();
                    break;
                case 2:
                    withdraw();
                    break;
                case 3:
                    checkBalance();
                    break;
                case 4:
                    showTransactionHistory();
                    break;
                case 5:
                    transferMoney();
                    break;
                case 6:
                    changePin();
                    break;
                case 7:
                    System.out.println("🔒 Logging out...");
                    currentAccount = null;
                    return;
                default:
                    System.out.println("❌ Invalid option!");
            }
        }
    }
    
    private static void displayAccountMenu() {
        System.out.println("\n┌─────────────────────────────────┐");
        System.out.println("│         ACCOUNT MENU             │");
        System.out.println("├─────────────────────────────────┤");
        System.out.println("│ 1. Deposit                      │");
        System.out.println("│ 2. Withdraw                     │");
        System.out.println("│ 3. Check Balance                │");
        System.out.println("│ 4. Transaction History          │");
        System.out.println("│ 5. Transfer Money               │");
        System.out.println("│ 6. Change PIN                   │");
        System.out.println("│ 7. Logout                       │");
        System.out.println("└─────────────────────────────────┘");
    }
    
    private static void deposit() {
        double amount = getDoubleInput("Enter amount to deposit: $");
        System.out.print("Enter description (optional): ");
        String desc = scanner.nextLine();
        
        double currentBalance = db.getBalance(currentAccount);
        if (db.updateBalance(currentAccount, currentBalance + amount)) {
            db.addTransaction(currentAccount, "DEPOSIT", amount, desc.isEmpty() ? "Deposit" : desc, null);
            System.out.printf("✅ Successfully deposited $%.2f\n", amount);
        }
    }
    
    private static void withdraw() {
        double amount = getDoubleInput("Enter amount to withdraw: $");
        double currentBalance = db.getBalance(currentAccount);
        
        if (amount > currentBalance) {
            System.out.printf("❌ Insufficient funds! Your balance is $%.2f\n", currentBalance);
            return;
        }
        
        System.out.print("Enter description (optional): ");
        String desc = scanner.nextLine();
        
        if (db.updateBalance(currentAccount, currentBalance - amount)) {
            db.addTransaction(currentAccount, "WITHDRAWAL", amount, desc.isEmpty() ? "Withdrawal" : desc, null);
            System.out.printf("✅ Successfully withdrew $%.2f\n", amount);
        }
    }
    
    private static void checkBalance() {
        double balance = db.getBalance(currentAccount);
        System.out.printf("\n💰 Your current balance: $%,.2f\n", balance);
    }
    
    private static void showTransactionHistory() {
        java.util.ArrayList<java.util.HashMap<String, String>> transactions = db.getTransactionHistory(currentAccount, 20);
        
        if (transactions.isEmpty()) {
            System.out.println("No transactions yet.");
            return;
        }
        
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                    TRANSACTION HISTORY                        ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        for (java.util.HashMap<String, String> t : transactions) {
            String date = t.get("date");
            if (date.length() > 19) date = date.substring(0, 19);
            System.out.printf("║ %-19s | %-10s | $%8s | %-25s║\n", 
                date, t.get("type"), t.get("amount"), t.get("description"));
        }
        System.out.println("╚══════════════════════════════════════════════════════════════╝\n");
    }
    
    private static void transferMoney() {
        System.out.print("Enter recipient account number: ");
        String toAccount = scanner.nextLine();
        
        double amount = getDoubleInput("Enter amount to transfer: $");
        
        System.out.print("Enter your PIN to confirm: ");
        String pin = scanner.nextLine();
        
        if (db.transferMoney(currentAccount, toAccount, amount, pin)) {
            System.out.printf("✅ Successfully transferred $%.2f to account %s\n", amount, toAccount);
        } else {
            System.out.println("❌ Transfer failed! Check account number, PIN, or balance.");
        }
    }
    
    private static void changePin() {
        System.out.print("Enter current PIN: ");
        String oldPin = scanner.nextLine();
        System.out.print("Enter new 4-digit PIN: ");
        String newPin = scanner.nextLine();
        
        if (db.changePin(currentAccount, oldPin, newPin)) {
            System.out.println("✅ PIN changed successfully!");
        } else {
            System.out.println("❌ Incorrect current PIN!");
        }
    }
    
    private static void displayAccountInfo(String accountNumber) {
        java.util.HashMap<String, Object> account = db.getAccountDetails(accountNumber);
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║          ACCOUNT INFORMATION          ║");
        System.out.println("╠════════════════════════════════════════╣");
        System.out.println("║ Account Number: " + account.get("account_number"));
        System.out.println("║ Account Holder: " + account.get("account_holder_name"));
        System.out.printf("║ Balance: $%,.2f%33s\n", account.get("balance"), "║");
        System.out.println("╚════════════════════════════════════════╝\n");
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