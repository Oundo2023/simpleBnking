<?php
session_start();
include 'db_connection.php';

if (!isset($_SESSION['accountNumber'])) {
    header("Location: index.php");
    exit();
}

$accountNumber = $_SESSION['accountNumber'];

// Handle recurring payment setup
if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    if (isset($_POST['create_recurring'])) {
        $payee = $_POST['payee_account'];
        $amount = $_POST['amount'];
        $frequency = $_POST['frequency'];
        $next_date = $_POST['next_date'];
        $description = $_POST['description'];
        
        $stmt = $conn->prepare("INSERT INTO recurring_payments (account_number, payee_account, amount, frequency, next_payment_date, description) VALUES (?, ?, ?, ?, ?, ?)");
        $stmt->bind_param("ssdsss", $accountNumber, $payee, $amount, $frequency, $next_date, $description);
        $stmt->execute();
        $_SESSION['message'] = "✅ Recurring payment scheduled!";
    } elseif (isset($_POST['cancel_recurring'])) {
        $id = $_POST['recurring_id'];
        $conn->query("DELETE FROM recurring_payments WHERE id = $id");
        $_SESSION['message'] = "Recurring payment cancelled!";
    }
}

$recurring = $conn->query("SELECT * FROM recurring_payments WHERE account_number = '$accountNumber' ORDER BY next_payment_date ASC");
?>

<!DOCTYPE html>
<html>
<head>
    <title>Recurring Payments - Simple Banking</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #f0f2f5; padding: 20px; }
        .container { max-width: 800px; margin: 0 auto; }
        .payment-card { background: white; border-radius: 15px; padding: 20px; margin-bottom: 20px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        input, select { width: 100%; padding: 12px; margin: 10px 0; border: 1px solid #ddd; border-radius: 8px; }
        button { background: #667eea; color: white; padding: 12px 20px; border: none; border-radius: 8px; cursor: pointer; }
        .btn-danger { background: #f56565; }
        .btn-back { background: #718096; margin-bottom: 20px; }
        .message { background: #c6f6d5; color: #22543d; padding: 10px; border-radius: 10px; margin-bottom: 20px; }
        h2, h3 { margin-bottom: 15px; }
        .recurring-item { border-bottom: 1px solid #e2e8f0; padding: 15px 0; }
    </style>
</head>
<body>
    <div class="container">
        <button class="btn-back" onclick="location.href='dashboard.php'">← Back to Dashboard</button>
        
        <?php if(isset($_SESSION['message'])): ?>
            <div class="message"><?php echo $_SESSION['message']; unset($_SESSION['message']); ?></div>
        <?php endif; ?>
        
        <div class="payment-card">
            <h2>🔄 Setup Recurring Payment</h2>
            <form method="POST">
                <input type="text" name="payee_account" placeholder="Payee Account Number" required>
                <input type="number" name="amount" placeholder="Amount ($)" step="0.01" required>
                <select name="frequency" required>
                    <option value="daily">Daily</option>
                    <option value="weekly">Weekly</option>
                    <option value="monthly">Monthly</option>
                </select>
                <input type="date" name="next_date" required>
                <input type="text" name="description" placeholder="Description">
                <button type="submit" name="create_recurring">Schedule Payment</button>
            </form>
        </div>
        
        <div class="payment-card">
            <h2>📅 Your Scheduled Payments</h2>
            <?php while($payment = $recurring->fetch_assoc()): ?>
                <div class="recurring-item">
                    <p><strong>To:</strong> <?php echo $payment['payee_account']; ?></p>
                    <p><strong>Amount:</strong> $<?php echo number_format($payment['amount'], 2); ?></p>
                    <p><strong>Frequency:</strong> <?php echo ucfirst($payment['frequency']); ?></p>
                    <p><strong>Next Payment:</strong> <?php echo $payment['next_payment_date']; ?></p>
                    <p><strong>Description:</strong> <?php echo $payment['description']; ?></p>
                    <form method="POST" style="margin-top: 10px;">
                        <input type="hidden" name="recurring_id" value="<?php echo $payment['id']; ?>">
                        <button type="submit" name="cancel_recurring" class="btn-danger">Cancel Payment</button>
                    </form>
                </div>
            <?php endwhile; ?>
            <?php if($recurring->num_rows == 0): ?>
                <p style="text-align:center; color:#999;">No scheduled payments</p>
            <?php endif; ?>
        </div>
    </div>
</body>
</html>