<?php
session_start();
include 'db_connection.php';

if (!isset($_SESSION['accountNumber'])) {
    header("Location: index.php");
    exit();
}

$accountNumber = $_SESSION['accountNumber'];
$action = $_POST['action'];

switch($action) {
    case 'deposit':
        $amount = $_POST['amount'];
        $desc = $_POST['description'] ?: 'Deposit';
        
        // Get current balance
        $result = $conn->query("SELECT balance FROM accounts WHERE account_number = '$accountNumber'");
        $balance = $result->fetch_assoc()['balance'];
        $newBalance = $balance + $amount;
        
        // Update balance
        $conn->query("UPDATE accounts SET balance = $newBalance WHERE account_number = '$accountNumber'");
        
        // Add transaction
        $conn->query("INSERT INTO transactions (account_number, transaction_type, amount, description) 
                      VALUES ('$accountNumber', 'DEPOSIT', $amount, '$desc')");
        
        $_SESSION['message'] = "✅ Deposited $" . number_format($amount, 2);
        break;
        
    case 'withdraw':
        $amount = $_POST['amount'];
        $desc = $_POST['description'] ?: 'Withdrawal';
        
        // Get current balance
        $result = $conn->query("SELECT balance FROM accounts WHERE account_number = '$accountNumber'");
        $balance = $result->fetch_assoc()['balance'];
        
        if ($amount > $balance) {
            $_SESSION['message'] = "❌ Insufficient funds!";
        } else {
            $newBalance = $balance - $amount;
            $conn->query("UPDATE accounts SET balance = $newBalance WHERE account_number = '$accountNumber'");
            $conn->query("INSERT INTO transactions (account_number, transaction_type, amount, description) 
                          VALUES ('$accountNumber', 'WITHDRAWAL', $amount, '$desc')");
            $_SESSION['message'] = "✅ Withdrew $" . number_format($amount, 2);
        }
        break;
        
    case 'transfer':
        $toAccount = $_POST['toAccount'];
        $amount = $_POST['amount'];
        $pin = $_POST['pin'];
        
        // Verify PIN
        $result = $conn->query("SELECT pin FROM accounts WHERE account_number = '$accountNumber'");
        $storedPin = $result->fetch_assoc()['pin'];
        
        if ($pin != $storedPin) {
            $_SESSION['message'] = "❌ Incorrect PIN!";
            break;
        }
        
        // Get balances
        $fromBalance = $conn->query("SELECT balance FROM accounts WHERE account_number = '$accountNumber'")->fetch_assoc()['balance'];
        $toBalance = $conn->query("SELECT balance FROM accounts WHERE account_number = '$toAccount'")->fetch_assoc()['balance'];
        
        if (!$toBalance) {
            $_SESSION['message'] = "❌ Recipient account not found!";
        } elseif ($amount > $fromBalance) {
            $_SESSION['message'] = "❌ Insufficient funds!";
        } else {
            // Update balances
            $conn->query("UPDATE accounts SET balance = " . ($fromBalance - $amount) . " WHERE account_number = '$accountNumber'");
            $conn->query("UPDATE accounts SET balance = " . ($toBalance + $amount) . " WHERE account_number = '$toAccount'");
            
            // Add transactions
            $conn->query("INSERT INTO transactions (account_number, transaction_type, amount, description, related_account) 
                          VALUES ('$accountNumber', 'TRANSFER_SENT', $amount, 'Transfer to $toAccount', '$toAccount')");
            $conn->query("INSERT INTO transactions (account_number, transaction_type, amount, description, related_account) 
                          VALUES ('$toAccount', 'TRANSFER_RECEIVED', $amount, 'Transfer from $accountNumber', '$accountNumber')");
            
            $_SESSION['message'] = "✅ Transferred $" . number_format($amount, 2) . " to account $toAccount";
        }
        break;
        
    case 'changepin':
        $oldPin = $_POST['oldPin'];
        $newPin = $_POST['newPin'];
        
        $result = $conn->query("SELECT pin FROM accounts WHERE account_number = '$accountNumber'");
        $storedPin = $result->fetch_assoc()['pin'];
        
        if ($oldPin != $storedPin) {
            $_SESSION['message'] = "❌ Incorrect current PIN!";
        } else {
            $conn->query("UPDATE accounts SET pin = '$newPin' WHERE account_number = '$accountNumber'");
            $_SESSION['message'] = "✅ PIN changed successfully!";
        }
        break;
}

header("Location: dashboard.php");
$conn->close();
?>