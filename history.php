<?php
session_start();
include 'db_connection.php';

if (!isset($_SESSION['accountNumber'])) {
    header("Location: index.php");
    exit();
}

$accountNumber = $_SESSION['accountNumber'];
$sql = "SELECT * FROM transactions WHERE account_number = '$accountNumber' ORDER BY transaction_date DESC";
$transactions = $conn->query($sql);
?>

<!DOCTYPE html>
<html>
<head>
    <title>Transaction History - Simple Banking</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: #f0f2f5;
            padding: 20px;
        }
        
        .container {
            max-width: 1200px;
            margin: 0 auto;
        }
        
        h1 {
            color: #667eea;
            margin-bottom: 20px;
        }
        
        table {
            width: 100%;
            background: white;
            border-radius: 15px;
            overflow: hidden;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        
        th {
            background: #667eea;
            color: white;
            padding: 15px;
            text-align: left;
        }
        
        td {
            padding: 12px 15px;
            border-bottom: 1px solid #e2e8f0;
        }
        
        .back-btn {
            background: #718096;
            color: white;
            border: none;
            padding: 10px 20px;
            border-radius: 8px;
            cursor: pointer;
            margin-bottom: 20px;
        }
        
        .deposit {
            color: #48bb78;
            font-weight: bold;
        }
        
        .withdrawal {
            color: #f56565;
            font-weight: bold;
        }
    </style>
</head>
<body>
    <div class="container">
        <button class="back-btn" onclick="location.href='dashboard.php'">← Back to Dashboard</button>
        <h1>📜 Transaction History</h1>
        
        <table>
            <thead>
                <tr>
                    <th>Date</th>
                    <th>Type</th>
                    <th>Amount</th>
                    <th>Description</th>
                </tr>
            </thead>
            <tbody>
                <?php while($trans = $transactions->fetch_assoc()): ?>
                    <tr>
                        <td><?php echo $trans['transaction_date']; ?></td>
                        <td class="<?php echo $trans['transaction_type'] == 'DEPOSIT' ? 'deposit' : 'withdrawal'; ?>">
                            <?php echo $trans['transaction_type']; ?>
                        </td>
                        <td>$<?php echo number_format($trans['amount'], 2); ?></td>
                        <td><?php echo $trans['description']; ?></td>
                    </tr>
                <?php endwhile; ?>
                <?php if($transactions->num_rows == 0): ?>
                    <tr>
                        <td colspan="4" style="text-align:center;">No transactions yet</td>
                    </tr>
                <?php endif; ?>
            </tbody>
        </table>
    </div>
</body>
</html>