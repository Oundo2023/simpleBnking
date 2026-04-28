<?php
session_start();
include 'db_connection.php';

if (!isset($_SESSION['accountNumber'])) {
    header("Location: index.php");
    exit();
}

$accountNumber = $_SESSION['accountNumber'];
$account = $conn->query("SELECT * FROM accounts WHERE account_number = '$accountNumber'")->fetch_assoc();

// Generate QR code data
$qrData = "BANKING::" . $accountNumber . "::" . $account['account_holder_name'];

// Handle payment
if ($_SERVER['REQUEST_METHOD'] == 'POST' && isset($_POST['make_payment'])) {
    $toAccount = $_POST['to_account'];
    $amount = $_POST['amount'];
    $pin = $_POST['pin'];
    
    $result = $conn->query("SELECT pin FROM accounts WHERE account_number = '$accountNumber'");
    $storedPin = $result->fetch_assoc()['pin'];
    
    if ($pin != $storedPin) {
        $_SESSION['error'] = "❌ Incorrect PIN!";
    } else {
        $fromBalance = $conn->query("SELECT balance FROM accounts WHERE account_number = '$accountNumber'")->fetch_assoc()['balance'];
        $toBalance = $conn->query("SELECT balance FROM accounts WHERE account_number = '$toAccount'")->fetch_assoc()['balance'];
        
        if (!$toBalance) {
            $_SESSION['error'] = "❌ Recipient account not found!";
        } elseif ($amount > $fromBalance) {
            $_SESSION['error'] = "❌ Insufficient funds!";
        } else {
            $conn->query("UPDATE accounts SET balance = " . ($fromBalance - $amount) . " WHERE account_number = '$accountNumber'");
            $conn->query("UPDATE accounts SET balance = " . ($toBalance + $amount) . " WHERE account_number = '$toAccount'");
            $conn->query("INSERT INTO transactions (account_number, transaction_type, amount, description, related_account) VALUES ('$accountNumber', 'QR_PAYMENT_SENT', $amount, 'QR Payment to $toAccount', '$toAccount')");
            $conn->query("INSERT INTO transactions (account_number, transaction_type, amount, description, related_account) VALUES ('$toAccount', 'QR_PAYMENT_RECEIVED', $amount, 'QR Payment from $accountNumber', '$accountNumber')");
            $_SESSION['message'] = "✅ QR Payment of $amount sent to $toAccount!";
        }
    }
}
?>

<!DOCTYPE html>
<html>
<head>
    <title>QR Payments - Simple Banking</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #f0f2f5; padding: 20px; }
        .container { max-width: 600px; margin: 0 auto; }
        .qr-card { background: white; border-radius: 15px; padding: 30px; margin-bottom: 20px; text-align: center; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        .qr-code { font-size: 200px; margin: 20px; }
        .payment-card { background: white; border-radius: 15px; padding: 20px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        input, select { width: 100%; padding: 12px; margin: 10px 0; border: 1px solid #ddd; border-radius: 8px; }
        button { width: 100%; background: #667eea; color: white; padding: 12px; border: none; border-radius: 8px; cursor: pointer; font-size: 16px; }
        .btn-back { background: #718096; margin-bottom: 20px; }
        .message { background: #c6f6d5; color: #22543d; padding: 10px; border-radius: 10px; margin-bottom: 20px; }
        .error { background: #fed7d7; color: #742a2a; }
        h2 { margin-bottom: 20px; text-align: center; }
    </style>
</head>
<body>
    <div class="container">
        <button class="btn-back" onclick="location.href='dashboard.php'">← Back to Dashboard</button>
        
        <div class="qr-card">
            <h2>📱 Your QR Code</h2>
            <div class="qr-code">🏦</div>
            <p>Account: <?php echo $accountNumber; ?></p>
            <p>Name: <?php echo $account['account_holder_name']; ?></p>
            <p>Scan to pay me</p>
        </div>
        
        <?php if(isset($_SESSION['message'])): ?>
            <div class="message"><?php echo $_SESSION['message']; unset($_SESSION['message']); ?></div>
        <?php endif; ?>
        <?php if(isset($_SESSION['error'])): ?>
            <div class="message error"><?php echo $_SESSION['error']; unset($_SESSION['error']); ?></div>
        <?php endif; ?>
        
        <div class="payment-card">
            <h2>📷 Scan & Pay</h2>
            <p>Enter account number to send money:</p>
            <form method="POST">
                <input type="text" name="to_account" placeholder="Recipient Account Number" required>
                <input type="number" name="amount" placeholder="Amount ($)" step="0.01" required>
                <input type="password" name="pin" placeholder="Your PIN" maxlength="4" required>
                <button type="submit" name="make_payment">📷 Scan & Pay</button>
            </form>
        </div>
    </div>
</body>
</html>