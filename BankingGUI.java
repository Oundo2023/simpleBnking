import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

public class BankingGUI extends JFrame {
    private static HashMap<String, BankAccount> accounts = new HashMap<>();
    private BankAccount currentAccount;
    
    private CardLayout cardLayout;
    private JPanel mainPanel;
    
    private JTextField loginAccountField;
    private JPasswordField loginPinField;
    
    private JTextField createAccountField;
    private JTextField createNameField;
    private JPasswordField createPinField;
    private JTextField createDepositField;
    
    private JLabel welcomeLabel;
    private JLabel balanceLabel;
    private JLabel accountNumberLabel;
    
    private DefaultTableModel tableModel;
    private JTable transactionTable;
    
    public BankingGUI() {
        setTitle("🏦 Simple Banking System");
        setSize(550, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        loadSavedAccounts();
        setupUI();
        
        // Shutdown hook to save on exit
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (BankAccount acc : accounts.values()) {
                acc.saveToFile();
            }
        }));
    }
    
    private void loadSavedAccounts() {
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
                        
                        BankAccount account = new BankAccount(accNumber, holderName, balance, pin);
                        accounts.put(accNumber, account);
                    } catch (IOException e) {
                        // Skip
                    }
                }
            }
        }
    }
    
    private void setupUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}
        
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        mainPanel.add(createLoginPanel(), "login");
        mainPanel.add(createRegisterPanel(), "register");
        mainPanel.add(createDashboardPanel(), "dashboard");
        mainPanel.add(createTransactionHistoryPanel(), "history");
        
        add(mainPanel);
        cardLayout.show(mainPanel, "login");
    }
    
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(33, 150, 243));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        JLabel titleLabel = new JLabel("🏦 SIMPLE BANKING SYSTEM");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);
        
        JLabel subtitleLabel = new JLabel("Secure Banking at Your Fingertips");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.WHITE);
        gbc.gridy = 1;
        panel.add(subtitleLabel, gbc);
        
        JPanel loginBox = new JPanel(new GridBagLayout());
        loginBox.setBackground(Color.WHITE);
        loginBox.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        
        GridBagConstraints loginGbc = new GridBagConstraints();
        loginGbc.insets = new Insets(10, 10, 10, 10);
        
        JLabel loginTitle = new JLabel("Login to Your Account");
        loginTitle.setFont(new Font("Arial", Font.BOLD, 18));
        loginTitle.setForeground(new Color(33, 150, 243));
        loginGbc.gridx = 0;
        loginGbc.gridy = 0;
        loginGbc.gridwidth = 2;
        loginBox.add(loginTitle, loginGbc);
        
        loginGbc.gridwidth = 1;
        loginGbc.gridy = 1;
        loginGbc.gridx = 0;
        loginBox.add(new JLabel("Account Number:"), loginGbc);
        loginAccountField = new JTextField(15);
        loginGbc.gridx = 1;
        loginBox.add(loginAccountField, loginGbc);
        
        loginGbc.gridy = 2;
        loginGbc.gridx = 0;
        loginBox.add(new JLabel("PIN:"), loginGbc);
        loginPinField = new JPasswordField(15);
        loginGbc.gridx = 1;
        loginBox.add(loginPinField, loginGbc);
        
        JButton loginBtn = new JButton("LOGIN");
        loginBtn.setBackground(new Color(76, 175, 80));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(new Font("Arial", Font.BOLD, 14));
        loginBtn.addActionListener(e -> performLogin());
        loginGbc.gridy = 3;
        loginGbc.gridx = 0;
        loginGbc.gridwidth = 2;
        loginBox.add(loginBtn, loginGbc);
        
        gbc.gridy = 2;
        gbc.gridx = 0;
        panel.add(loginBox, gbc);
        
        JButton registerBtn = new JButton("Don't have an account? CREATE ONE");
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setBackground(new Color(33, 150, 243));
        registerBtn.setBorderPainted(false);
        registerBtn.addActionListener(e -> cardLayout.show(mainPanel, "register"));
        gbc.gridy = 3;
        panel.add(registerBtn, gbc);
        
        return panel;
    }
    
    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(33, 150, 243));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        JLabel titleLabel = new JLabel("Create New Account");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(titleLabel, gbc);
        
        JPanel registerBox = new JPanel(new GridBagLayout());
        registerBox.setBackground(Color.WHITE);
        
        GridBagConstraints regGbc = new GridBagConstraints();
        regGbc.insets = new Insets(8, 8, 8, 8);
        
        regGbc.gridy = 0;
        regGbc.gridx = 0;
        registerBox.add(new JLabel("Account Number:"), regGbc);
        createAccountField = new JTextField(15);
        regGbc.gridx = 1;
        registerBox.add(createAccountField, regGbc);
        
        regGbc.gridy = 1;
        regGbc.gridx = 0;
        registerBox.add(new JLabel("Full Name:"), regGbc);
        createNameField = new JTextField(15);
        regGbc.gridx = 1;
        registerBox.add(createNameField, regGbc);
        
        regGbc.gridy = 2;
        regGbc.gridx = 0;
        registerBox.add(new JLabel("4-digit PIN:"), regGbc);
        createPinField = new JPasswordField(15);
        regGbc.gridx = 1;
        registerBox.add(createPinField, regGbc);
        
        regGbc.gridy = 3;
        regGbc.gridx = 0;
        registerBox.add(new JLabel("Initial Deposit:"), regGbc);
        createDepositField = new JTextField(15);
        regGbc.gridx = 1;
        registerBox.add(createDepositField, regGbc);
        
        JButton createBtn = new JButton("CREATE ACCOUNT");
        createBtn.setBackground(new Color(76, 175, 80));
        createBtn.setForeground(Color.WHITE);
        createBtn.setFont(new Font("Arial", Font.BOLD, 14));
        createBtn.addActionListener(e -> createAccount());
        regGbc.gridy = 4;
        regGbc.gridx = 0;
        regGbc.gridwidth = 2;
        registerBox.add(createBtn, regGbc);
        
        gbc.gridy = 1;
        panel.add(registerBox, gbc);
        
        JButton backBtn = new JButton("← Back to Login");
        backBtn.setForeground(Color.WHITE);
        backBtn.setBackground(new Color(33, 150, 243));
        backBtn.setBorderPainted(false);
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "login"));
        gbc.gridy = 2;
        panel.add(backBtn, gbc);
        
        return panel;
    }
    
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(240, 248, 255));
        
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(33, 150, 243));
        headerPanel.setLayout(new GridBagLayout());
        
        GridBagConstraints headerGbc = new GridBagConstraints();
        headerGbc.insets = new Insets(15, 15, 15, 15);
        
        welcomeLabel = new JLabel("Welcome, User");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        welcomeLabel.setForeground(Color.WHITE);
        headerGbc.gridx = 0;
        headerGbc.gridy = 0;
        headerPanel.add(welcomeLabel, headerGbc);
        
        accountNumberLabel = new JLabel("Account: ****");
        accountNumberLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        accountNumberLabel.setForeground(Color.WHITE);
        headerGbc.gridy = 1;
        headerPanel.add(accountNumberLabel, headerGbc);
        
        balanceLabel = new JLabel("$0.00");
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 32));
        balanceLabel.setForeground(Color.WHITE);
        headerGbc.gridy = 2;
        headerPanel.add(balanceLabel, headerGbc);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        JPanel buttonPanel = new JPanel(new GridLayout(4, 2, 15, 15));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        buttonPanel.setBackground(new Color(240, 248, 255));
        
        String[] buttons = {"💰 DEPOSIT", "💸 WITHDRAW", "📊 CHECK BALANCE", "📜 HISTORY", "🔄 TRANSFER", "🔐 CHANGE PIN", "📈 INTEREST", "🚪 LOGOUT"};
        
        for (String btnText : buttons) {
            JButton btn = new JButton(btnText);
            btn.setFont(new Font("Arial", Font.BOLD, 13));
            btn.setBackground(Color.WHITE);
            btn.setForeground(new Color(33, 150, 243));
            btn.setBorder(BorderFactory.createLineBorder(new Color(33, 150, 243), 1));
            
            switch (btnText) {
                case "💰 DEPOSIT": btn.addActionListener(e -> showDepositDialog()); break;
                case "💸 WITHDRAW": btn.addActionListener(e -> showWithdrawDialog()); break;
                case "📊 CHECK BALANCE": btn.addActionListener(e -> showBalance()); break;
                case "📜 HISTORY": btn.addActionListener(e -> showTransactionHistory()); break;
                case "🔄 TRANSFER": btn.addActionListener(e -> showTransferDialog()); break;
                case "🔐 CHANGE PIN": btn.addActionListener(e -> showChangePinDialog()); break;
                case "📈 INTEREST": btn.addActionListener(e -> showInterestDialog()); break;
                case "🚪 LOGOUT": btn.addActionListener(e -> logout()); break;
            }
            buttonPanel.add(btn);
        }
        
        panel.add(buttonPanel, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createTransactionHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        String[] columns = {"Date", "Type", "Amount", "Description"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        transactionTable = new JTable(tableModel);
        transactionTable.setFont(new Font("Arial", Font.PLAIN, 12));
        transactionTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        JScrollPane scrollPane = new JScrollPane(transactionTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Color.WHITE);
        
        JButton exportBtn = new JButton("📎 Export to CSV");
        exportBtn.addActionListener(e -> {
            if (currentAccount != null) {
                currentAccount.exportToCSV();
                JOptionPane.showMessageDialog(this, "✅ Transactions exported to CSV!");
            }
        });
        
        JButton backBtn = new JButton("← Back");
        backBtn.addActionListener(e -> {
            updateDashboard();
            cardLayout.show(mainPanel, "dashboard");
        });
        
        bottomPanel.add(exportBtn);
        bottomPanel.add(backBtn);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void performLogin() {
        String accNumber = loginAccountField.getText().trim();
        String pin = new String(loginPinField.getPassword());
        
        BankAccount account = accounts.get(accNumber);
        if (account != null && account.verifyPin(pin)) {
            currentAccount = account;
            updateDashboard();
            cardLayout.show(mainPanel, "dashboard");
            loginAccountField.setText("");
            loginPinField.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "❌ Invalid account number or PIN!", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void createAccount() {
        String accNumber = createAccountField.getText().trim();
        String name = createNameField.getText().trim();
        String pin = new String(createPinField.getPassword());
        
        if (accNumber.isEmpty() || name.isEmpty() || pin.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields!");
            return;
        }
        
        double deposit;
        try {
            deposit = Double.parseDouble(createDepositField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid deposit amount!");
            return;
        }
        
        if (accounts.containsKey(accNumber)) {
            JOptionPane.showMessageDialog(this, "Account number already exists!");
        } else if (pin.length() != 4 || !pin.matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "PIN must be exactly 4 digits!");
        } else if (deposit < 0) {
            JOptionPane.showMessageDialog(this, "Initial deposit cannot be negative!");
        } else {
            BankAccount newAccount = new BankAccount(accNumber, name, deposit, pin);
            accounts.put(accNumber, newAccount);
            newAccount.saveToFile();
            JOptionPane.showMessageDialog(this, "✅ Account created successfully!");
            
            createAccountField.setText("");
            createNameField.setText("");
            createPinField.setText("");
            createDepositField.setText("");
            cardLayout.show(mainPanel, "login");
        }
    }
    
    private void updateDashboard() {
        if (currentAccount != null) {
            welcomeLabel.setText("Welcome, " + currentAccount.getAccountHolderName());
            accountNumberLabel.setText("Account: " + currentAccount.getAccountNumber());
            balanceLabel.setText(String.format("$%,.2f", currentAccount.getBalance()));
        }
    }
    
    private void showDepositDialog() {
        String amountStr = JOptionPane.showInputDialog(this, "Enter deposit amount:", "Deposit", JOptionPane.PLAIN_MESSAGE);
        if (amountStr != null) {
            try {
                double amount = Double.parseDouble(amountStr);
                String desc = JOptionPane.showInputDialog(this, "Description (optional):", "Deposit", JOptionPane.PLAIN_MESSAGE);
                if (currentAccount.deposit(amount, desc == null ? "" : desc)) {
                    currentAccount.saveToFile();
                    updateDashboard();
                    JOptionPane.showMessageDialog(this, "✅ Deposit successful!");
                } else {
                    JOptionPane.showMessageDialog(this, "❌ Invalid amount!");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid amount!");
            }
        }
    }
    
    private void showWithdrawDialog() {
        String amountStr = JOptionPane.showInputDialog(this, "Enter withdrawal amount:", "Withdraw", JOptionPane.PLAIN_MESSAGE);
        if (amountStr != null) {
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount > currentAccount.getBalance()) {
                    JOptionPane.showMessageDialog(this, "❌ Insufficient funds! Your balance: $" + currentAccount.getBalance());
                    return;
                }
                String desc = JOptionPane.showInputDialog(this, "Description (optional):", "Withdraw", JOptionPane.PLAIN_MESSAGE);
                if (currentAccount.withdraw(amount, desc == null ? "" : desc)) {
                    currentAccount.saveToFile();
                    updateDashboard();
                    JOptionPane.showMessageDialog(this, "✅ Withdrawal successful!");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid amount!");
            }
        }
    }
    
    private void showBalance() {
        JOptionPane.showMessageDialog(this, String.format("💰 Current Balance: $%,.2f", currentAccount.getBalance()), "Balance", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showTransactionHistory() {
        tableModel.setRowCount(0);
        // Simplified for demo - would need full transaction loading
        cardLayout.show(mainPanel, "history");
    }
    
    private void showTransferDialog() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        JTextField recipientField = new JTextField();
        JTextField amountField = new JTextField();
        JPasswordField pinField = new JPasswordField();
        
        panel.add(new JLabel("Recipient Account:"));
        panel.add(recipientField);
        panel.add(new JLabel("Amount:"));
        panel.add(amountField);
        panel.add(new JLabel("Your PIN:"));
        panel.add(pinField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Transfer Money", JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            String recipientNum = recipientField.getText().trim();
            String pin = new String(pinField.getPassword());
            
            try {
                double amount = Double.parseDouble(amountField.getText().trim());
                BankAccount recipient = accounts.get(recipientNum);
                
                if (recipient == null) {
                    JOptionPane.showMessageDialog(this, "Recipient account not found!");
                } else if (!currentAccount.verifyPin(pin)) {
                    JOptionPane.showMessageDialog(this, "Incorrect PIN!");
                } else if (amount > currentAccount.getBalance()) {
                    JOptionPane.showMessageDialog(this, "Insufficient funds!");
                } else if (currentAccount.withdraw(amount, "Transfer to " + recipientNum)) {
                    recipient.deposit(amount, "Transfer from " + currentAccount.getAccountNumber());
                    currentAccount.saveToFile();
                    recipient.saveToFile();
                    updateDashboard();
                    JOptionPane.showMessageDialog(this, "✅ Transfer successful!");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid amount!");
            }
        }
    }
    
    private void showChangePinDialog() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        JPasswordField oldPinField = new JPasswordField();
        JPasswordField newPinField = new JPasswordField();
        
        panel.add(new JLabel("Current PIN:"));
        panel.add(oldPinField);
        panel.add(new JLabel("New PIN:"));
        panel.add(newPinField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Change PIN", JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            String oldPin = new String(oldPinField.getPassword());
            String newPin = new String(newPinField.getPassword());
            
            if (currentAccount.changePin(oldPin, newPin)) {
                currentAccount.saveToFile();
                JOptionPane.showMessageDialog(this, "✅ PIN changed successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "❌ Incorrect current PIN!");
            }
        }
    }
    
    private void showInterestDialog() {
        String rateStr = JOptionPane.showInputDialog(this, "Enter annual interest rate (%):", "Calculate Interest", JOptionPane.PLAIN_MESSAGE);
        if (rateStr != null) {
            try {
                double rate = Double.parseDouble(rateStr);
                currentAccount.calculateInterest(rate);
                currentAccount.saveToFile();
                updateDashboard();
                JOptionPane.showMessageDialog(this, "✅ Interest added successfully!");
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid rate!");
            }
        }
    }
    
    private void logout() {
        currentAccount = null;
        cardLayout.show(mainPanel, "login");
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BankingGUI().setVisible(true));
    }
}