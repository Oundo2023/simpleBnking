import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class DatabaseConnection {
    // XAMPP default settings
    private static final String URL = "jdbc:mysql://localhost:3306/banking_system";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";  // XAMPP has no password by default
    
    private Connection connection;
    
    public DatabaseConnection() {
        try {
            // Load MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Establish connection
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("✅ Connected to XAMPP MySQL database!");
            
        } catch (ClassNotFoundException e) {
            System.out.println("❌ MySQL Driver not found!");
            System.out.println("Make sure mysql-connector-jar is in the lib folder");
            e.printStackTrace();
            
        } catch (SQLException e) {
            System.out.println("❌ Database connection failed!");
            System.out.println("Make sure XAMPP MySQL is running");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Verify login credentials
    public boolean verifyLogin(String accountNumber, String pin) {
        String query = "SELECT * FROM accounts WHERE account_number = ? AND pin = ? AND is_active = TRUE";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, accountNumber);
            stmt.setString(2, pin);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Get account details
    public HashMap<String, Object> getAccountDetails(String accountNumber) {
        HashMap<String, Object> account = new HashMap<>();
        String query = "SELECT * FROM accounts WHERE account_number = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                account.put("account_number", rs.getString("account_number"));
                account.put("account_holder_name", rs.getString("account_holder_name"));
                account.put("balance", rs.getDouble("balance"));
                account.put("created_date", rs.getTimestamp("created_date"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return account;
    }
    
    // Get account balance
    public double getBalance(String accountNumber) {
        String query = "SELECT balance FROM accounts WHERE account_number = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("balance");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    // Update account balance
    public boolean updateBalance(String accountNumber, double newBalance) {
        String query = "UPDATE accounts SET balance = ? WHERE account_number = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDouble(1, newBalance);
            stmt.setString(2, accountNumber);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Add transaction record
    public boolean addTransaction(String accountNumber, String type, double amount, 
                                   String description, String relatedAccount) {
        String query = "INSERT INTO transactions (account_number, transaction_type, amount, description, related_account) "
                     + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, accountNumber);
            stmt.setString(2, type);
            stmt.setDouble(3, amount);
            stmt.setString(4, description);
            stmt.setString(5, relatedAccount);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Get transaction history
    public ArrayList<HashMap<String, String>> getTransactionHistory(String accountNumber, int limit) {
        ArrayList<HashMap<String, String>> transactions = new ArrayList<>();
        String query = "SELECT * FROM transactions WHERE account_number = ? ORDER BY transaction_date DESC LIMIT ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, accountNumber);
            stmt.setInt(2, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                HashMap<String, String> trans = new HashMap<>();
                trans.put("type", rs.getString("transaction_type"));
                trans.put("amount", String.valueOf(rs.getDouble("amount")));
                trans.put("description", rs.getString("description"));
                trans.put("date", rs.getTimestamp("transaction_date").toString());
                transactions.add(trans);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }
    
    // Create new account
    public boolean createAccount(String accountNumber, String holderName, String pin, double initialDeposit) {
        String query = "INSERT INTO accounts (account_number, account_holder_name, pin, balance) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, accountNumber);
            stmt.setString(2, holderName);
            stmt.setString(3, pin);
            stmt.setDouble(4, initialDeposit);
            boolean success = stmt.executeUpdate() > 0;
            
            if (success && initialDeposit > 0) {
                addTransaction(accountNumber, "DEPOSIT", initialDeposit, "Initial deposit", null);
            }
            return success;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Transfer money between accounts
    public boolean transferMoney(String fromAccount, String toAccount, double amount, String pin) {
        // First verify PIN
        if (!verifyLogin(fromAccount, pin)) {
            return false;
        }
        
        // Get current balances
        double fromBalance = getBalance(fromAccount);
        double toBalance = getBalance(toAccount);
        
        if (fromBalance < amount) {
            return false;
        }
        
        // Start transaction (atomic operation)
        try {
            connection.setAutoCommit(false);
            
            // Update sender balance
            if (!updateBalance(fromAccount, fromBalance - amount)) {
                connection.rollback();
                return false;
            }
            
            // Update recipient balance
            if (!updateBalance(toAccount, toBalance + amount)) {
                connection.rollback();
                return false;
            }
            
            // Record transactions
            addTransaction(fromAccount, "TRANSFER_SENT", amount, "Transfer to " + toAccount, toAccount);
            addTransaction(toAccount, "TRANSFER_RECEIVED", amount, "Transfer from " + fromAccount, fromAccount);
            
            connection.commit();
            return true;
            
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    // Change account PIN
    public boolean changePin(String accountNumber, String oldPin, String newPin) {
        if (!verifyLogin(accountNumber, oldPin)) {
            return false;
        }
        
        String query = "UPDATE accounts SET pin = ? WHERE account_number = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, newPin);
            stmt.setString(2, accountNumber);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Get all accounts (Admin function)
    public ArrayList<HashMap<String, Object>> getAllAccounts() {
        ArrayList<HashMap<String, Object>> accounts = new ArrayList<>();
        String query = "SELECT account_number, account_holder_name, balance, created_date FROM accounts WHERE is_active = TRUE";
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                HashMap<String, Object> acc = new HashMap<>();
                acc.put("account_number", rs.getString("account_number"));
                acc.put("account_holder_name", rs.getString("account_holder_name"));
                acc.put("balance", rs.getDouble("balance"));
                acc.put("created_date", rs.getTimestamp("created_date"));
                accounts.add(acc);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return accounts;
    }
    
    // Get total bank balance (Admin function)
    public double getTotalBankBalance() {
        String query = "SELECT SUM(balance) as total FROM accounts WHERE is_active = TRUE";
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    // Delete account (soft delete - Admin function)
    public boolean deleteAccount(String accountNumber) {
        String query = "UPDATE accounts SET is_active = FALSE WHERE account_number = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, accountNumber);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Close database connection
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✅ Database connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}