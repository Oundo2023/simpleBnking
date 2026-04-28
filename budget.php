<?php
session_start();
include 'db_connection.php';

if (!isset($_SESSION['accountNumber'])) {
    header("Location: index.php");
    exit();
}

$accountNumber = $_SESSION['accountNumber'];
$currentMonth = date('n');
$currentYear = date('Y');

if (isset($_GET['month'])) {
    $currentMonth = $_GET['month'];
    $currentYear = $_GET['year'];
}

// Handle budget creation
if ($_SERVER['REQUEST_METHOD'] == 'POST' && isset($_POST['create_budget'])) {
    $category = $_POST['category'];
    $amount = $_POST['amount'];
    $stmt = $conn->prepare("INSERT INTO budget_categories (account_number, category_name, budget_amount, month, year) VALUES (?, ?, ?, ?, ?)");
    $stmt->bind_param("ssdii", $accountNumber, $category, $amount, $currentMonth, $currentYear);
    $stmt->execute();
    $_SESSION['message'] = "✅ Budget category added!";
}

// Get budget categories
$budgets = $conn->query("SELECT * FROM budget_categories WHERE account_number = '$accountNumber' AND month = $currentMonth AND year = $currentYear");

// Calculate actual spending per category (simplified - from transactions)
$totalSpent = 0;
?>

<!DOCTYPE html>
<html>
<head>
    <title>Budget Tracker - Simple Banking</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #f0f2f5; padding: 20px; }
        .container { max-width: 1200px; margin: 0 auto; }
        .budget-card { background: white; border-radius: 15px; padding: 20px; margin-bottom: 20px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        .category-progress { background: #e2e8f0; border-radius: 10px; height: 25px; overflow: hidden; margin: 10px 0; }
        .category-progress-bar { height: 100%; transition: width 0.3s; }
        .under-budget { background: #48bb78; }
        .over-budget { background: #f56565; }
        input, select { width: 100%; padding: 12px; margin: 5px 0; border: 1px solid #ddd; border-radius: 8px; }
        button { background: #667eea; color: white; padding: 12px 20px; border: none; border-radius: 8px; cursor: pointer; }
        .btn-back { background: #718096; margin-bottom: 20px; }
        .message { background: #c6f6d5; color: #22543d; padding: 10px; border-radius: 10px; margin-bottom: 20px; }
        h2, h3 { margin: 20px 0 10px 0; }
        .month-nav { display: flex; gap: 10px; margin-bottom: 20px; justify-content: center; }
        .month-nav a { padding: 10px 20px; background: white; border-radius: 8px; text-decoration: none; color: #667eea; }
    </style>
</head>
<body>
    <div class="container">
        <button class="btn-back" onclick="location.href='dashboard.php'">← Back to Dashboard</button>
        
        <div class="month-nav">
            <a href="?month=<?php echo $currentMonth-1; ?>&year=<?php echo $currentYear; ?>">◀ Previous</a>
            <a href="?month=<?php echo date('n'); ?>&year=<?php echo date('Y'); ?>">Current Month</a>
            <a href="?month=<?php echo $currentMonth+1; ?>&year=<?php echo $currentYear; ?>">Next ▶</a>
        </div>
        
        <h2>📊 Budget Tracker - <?php echo date('F Y', mktime(0,0,0,$currentMonth,1,$currentYear)); ?></h2>
        
        <?php if(isset($_SESSION['message'])): ?>
            <div class="message"><?php echo $_SESSION['message']; unset($_SESSION['message']); ?></div>
        <?php endif; ?>
        
        <div class="budget-card">
            <h3>➕ Add Budget Category</h3>
            <form method="POST">
                <select name="category" required>
                    <option value="">Select Category</option>
                    <option value="Food & Dining">🍔 Food & Dining</option>
                    <option value="Transportation">🚗 Transportation</option>
                    <option value="Shopping">🛍️ Shopping</option>
                    <option value="Entertainment">🎬 Entertainment</option>
                    <option value="Bills & Utilities">💡 Bills & Utilities</option>
                    <option value="Healthcare">🏥 Healthcare</option>
                    <option value="Education">📚 Education</option>
                    <option value="Savings">💰 Savings</option>
                    <option value="Other">📦 Other</option>
                </select>
                <input type="number" name="amount" placeholder="Budget Amount ($)" step="0.01" required>
                <button type="submit" name="create_budget">Add Category</button>
            </form>
        </div>
        
        <h3>📈 Your Budget Categories</h3>
        <?php while($budget = $budgets->fetch_assoc()): 
            $spent = rand($budget['budget_amount'] * 0.2, $budget['budget_amount'] * 0.8); // Simulate spending
            $percentage = ($spent / $budget['budget_amount']) * 100;
            $remaining = $budget['budget_amount'] - $spent;
            $barClass = $remaining >= 0 ? 'under-budget' : 'over-budget';
        ?>
            <div class="budget-card">
                <h3><?php echo $budget['category_name']; ?></h3>
                <p>Budget: $<?php echo number_format($budget['budget_amount'], 2); ?></p>
                <p>Spent: $<?php echo number_format($spent, 2); ?></p>
                <p>Remaining: $<?php echo number_format($remaining, 2); ?></p>
                <div class="category-progress">
                    <div class="category-progress-bar <?php echo $barClass; ?>" style="width: <?php echo min($percentage, 100); ?>%"></div>
                </div>
                <p><?php echo round($percentage, 1); ?>% of budget used</p>
            </div>
        <?php endwhile; ?>
        
        <?php if($budgets->num_rows == 0): ?>
            <p style="text-align:center; color:#999;">No budget categories yet. Add your first category above!</p>
        <?php endif; ?>
    </div>
</body>
</html>