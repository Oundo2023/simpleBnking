<?php
// For Render.com deployment
$host = getenv('DB_HOST') ?: 'localhost';
$user = getenv('DB_USER') ?: 'root';
$password = getenv('DB_PASS') ?: '';
$database = getenv('DB_NAME') ?: 'banking_system';

$conn = new mysqli($host, $user, $password, $database);

if ($conn->connect_error) {
    // For Render - will create database on first run
    $conn = new mysqli($host, $user, $password);
    if (!$conn->connect_error) {
        $conn->query("CREATE DATABASE IF NOT EXISTS banking_system");
        $conn->select_db('banking_system');
        createTables($conn);
    }
}

function createTables($conn) {
    $conn->query("
        CREATE TABLE IF NOT EXISTS accounts (
            account_number VARCHAR(20) PRIMARY KEY,
            account_holder_name VARCHAR(100) NOT NULL,
            pin VARCHAR(4) NOT NULL,
            balance DECIMAL(15,2) DEFAULT 0.00,
            created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            is_active BOOLEAN DEFAULT TRUE
        )
    ");
    
    $conn->query("
        CREATE TABLE IF NOT EXISTS transactions (
            transaction_id INT PRIMARY KEY AUTO_INCREMENT,
            account_number VARCHAR(20) NOT NULL,
            transaction_type VARCHAR(20) NOT NULL,
            amount DECIMAL(15,2) NOT NULL,
            description TEXT,
            related_account VARCHAR(20),
            transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (account_number) REFERENCES accounts(account_number)
        )
    ");
    
    $conn->query("
        CREATE TABLE IF NOT EXISTS savings_goals (
            id INT PRIMARY KEY AUTO_INCREMENT,
            account_number VARCHAR(20),
            goal_name VARCHAR(100),
            target_amount DECIMAL(15,2),
            saved_amount DECIMAL(15,2) DEFAULT 0,
            deadline DATE,
            status VARCHAR(20) DEFAULT 'active',
            created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (account_number) REFERENCES accounts(account_number)
        )
    ");
    
    $conn->query("
        CREATE TABLE IF NOT EXISTS budget_categories (
            id INT PRIMARY KEY AUTO_INCREMENT,
            account_number VARCHAR(20),
            category_name VARCHAR(50),
            budget_amount DECIMAL(15,2),
            spent_amount DECIMAL(15,2) DEFAULT 0,
            month INT,
            year INT,
            FOREIGN KEY (account_number) REFERENCES accounts(account_number)
        )
    ");
    
    $conn->query("
        CREATE TABLE IF NOT EXISTS recurring_payments (
            id INT PRIMARY KEY AUTO_INCREMENT,
            account_number VARCHAR(20),
            payee_account VARCHAR(20),
            amount DECIMAL(15,2),
            frequency VARCHAR(20),
            next_payment_date DATE,
            description TEXT,
            is_active BOOLEAN DEFAULT TRUE,
            FOREIGN KEY (account_number) REFERENCES accounts(account_number)
        )
    ");
    
    // Add sample account if no accounts exist
    $check = $conn->query("SELECT COUNT(*) as count FROM accounts");
    $row = $check->fetch_assoc();
    if ($row['count'] == 0) {
        $conn->query("INSERT INTO accounts (account_number, account_holder_name, pin, balance) VALUES ('1001', 'Demo User', '1234', 5000)");
    }
}
?>