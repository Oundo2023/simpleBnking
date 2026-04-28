<?php
session_start();
include 'db_connection.php';

if (!isset($_SESSION['accountNumber'])) {
    header("Location: index.php");
    exit();
}

$accountNumber = $_SESSION['accountNumber'];

// Handle form submissions
if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    if (isset($_POST['create_goal'])) {
        $goal_name = $_POST['goal_name'];
        $target = $_POST['target_amount'];
        $deadline = $_POST['deadline'];
        $stmt = $conn->prepare("INSERT INTO savings_goals (account_number, goal_name, target_amount, deadline) VALUES (?, ?, ?, ?)");
        $stmt->bind_param("ssds", $accountNumber, $goal_name, $target, $deadline);
        $stmt->execute();
        $_SESSION['message'] = "✅ Savings goal created!";
    } elseif (isset($_POST['contribute'])) {
        $goal_id = $_POST['goal_id'];
        $amount = $_POST['amount'];
        
        // Get current balance
        $balance = $conn->query("SELECT balance FROM accounts WHERE account_number = '$accountNumber'")->fetch_assoc()['balance'];
        
        if ($amount <= $balance) {
            $conn->query("UPDATE accounts SET balance = balance - $amount WHERE account_number = '$accountNumber'");
            $conn->query("UPDATE savings_goals SET saved_amount = saved_amount + $amount WHERE id = $goal_id");
            $conn->query("INSERT INTO transactions (account_number, transaction_type, amount, description) VALUES ('$accountNumber', 'WITHDRAWAL', $amount, 'Savings: Contribution to goal')");
            $_SESSION['message'] = "✅ Added $amount to your savings goal!";
        } else {
            $_SESSION['error'] = "❌ Insufficient balance!";
        }
    } elseif (isset($_POST['delete_goal'])) {
        $goal_id = $_POST['goal_id'];
        $conn->query("DELETE FROM savings_goals WHERE id = $goal_id");
        $_SESSION['message'] = "Goal deleted!";
    }
}

$goals = $conn->query("SELECT * FROM savings_goals WHERE account_number = '$accountNumber' ORDER BY deadline ASC");
$balance = $conn->query("SELECT balance FROM accounts WHERE account_number = '$accountNumber'")->fetch_assoc()['balance'];
?>

<!DOCTYPE html>
<html>
<head>
    <title>Savings Goals - Simple Banking</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #f0f2f5; padding: 20px; }
        .container { max-width: 1200px; margin: 0 auto; }
        .balance-card { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; border-radius: 15px; margin-bottom: 20px; text-align: center; }
        .balance-amount { font-size: 36px; font-weight: bold; }
        .goal-card { background: white; border-radius: 15px; padding: 20px; margin-bottom: 20px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        .progress-bar { background: #e2e8f0; border-radius: 10px; height: 20px; overflow: hidden; margin: 10px 0; }
        .progress { background: #48bb78; height: 100%; transition: width 0.3s; }
        .form-group { margin: 10px 0; }
        input, select { width: 100%; padding: 12px; margin: 5px 0; border: 1px solid #ddd; border-radius: 8px; }
        button { background: #667eea; color: white; padding: 12px 20px; border: none; border-radius: 8px; cursor: pointer; margin: 5px; }
        .btn-danger { background: #f56565; }
        .btn-back { background: #718096; }
        .message { background: #c6f6d5; color: #22543d; padding: 10px; border-radius: 10px; margin-bottom: 20px; }
        .error { background: #fed7d7; color: #742a2a; }
        h2 { margin: 20px 0; }
    </style>
</head>
<body>
    <div class="container">
        <button class="btn-back" onclick="location.href='dashboard.php'">← Back to Dashboard</button>
        
        <div class="balance-card">
            <h3>Available Balance</h3>
            <div class="balance-amount">$<?php echo number_format($balance, 2); ?></div>
        </div>
        
        <?php if(isset($_SESSION['message'])): ?>
            <div class="message"><?php echo $_SESSION['message']; unset($_SESSION['message']); ?></div>
        <?php endif; ?>
        <?php if(isset($_SESSION['error'])): ?>
            <div class="message error"><?php echo $_SESSION['error']; unset($_SESSION['error']); ?></div>
        <?php endif; ?>
        
        <div class="goal-card">
            <h3>🎯 Create New Savings Goal</h3>
            <form method="POST">
                <div class="form-group">
                    <input type="text" name="goal_name" placeholder="Goal Name (e.g., New Car, Vacation)" required>
                </div>
                <div class="form-group">
                    <input type="number" name="target_amount" placeholder="Target Amount ($)" step="0.01" required>
                </div>
                <div class="form-group">
                    <input type="date" name="deadline" required>
                </div>
                <button type="submit" name="create_goal">Create Goal</button>
            </form>
        </div>
        
        <h2>📊 Your Savings Goals</h2>
        <?php while($goal = $goals->fetch_assoc()): 
            $percentage = ($goal['saved_amount'] / $goal['target_amount']) * 100;
            $remaining = $goal['target_amount'] - $goal['saved_amount'];
        ?>
            <div class="goal-card">
                <h3>💰 <?php echo htmlspecialchars($goal['goal_name']); ?></h3>
                <p>Target: $<?php echo number_format($goal['target_amount'], 2); ?></p>
                <p>Saved: $<?php echo number_format($goal['saved_amount'], 2); ?></p>
                <p>Remaining: $<?php echo number_format($remaining, 2); ?></p>
                <div class="progress-bar">
                    <div class="progress" style="width: <?php echo min($percentage, 100); ?>%"></div>
                </div>
                <p><?php echo round($percentage, 1); ?>% Complete</p>
                <p>Deadline: <?php echo $goal['deadline']; ?></p>
                <p>Days Left: <?php echo max(0, ceil((strtotime($goal['deadline']) - time()) / 86400)); ?> days</p>
                
                <form method="POST" style="margin-top: 15px;">
                    <input type="hidden" name="goal_id" value="<?php echo $goal['id']; ?>">
                    <input type="number" name="amount" placeholder="Amount to add" step="0.01" required>
                    <button type="submit" name="contribute">💵 Contribute</button>
                    <button type="submit" name="delete_goal" class="btn-danger" onclick="return confirm('Delete this goal?')">🗑️ Delete</button>
                </form>
            </div>
        <?php endwhile; ?>
        
        <?php if($goals->num_rows == 0): ?>
            <p style="text-align:center; color:#999;">No savings goals yet. Create your first goal above!</p>
        <?php endif; ?>
    </div>
</body>
</html>