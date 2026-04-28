<?php
session_start();
include 'db_connection.php';

if (!isset($_SESSION['accountNumber'])) {
    header("Location: index.php");
    exit();
}

$accountNumber = $_SESSION['accountNumber'];

// Get account details
$sql = "SELECT * FROM accounts WHERE account_number = '$accountNumber'";
$result = $conn->query($sql);
$account = $result->fetch_assoc();

// Get recent transactions
$trans_sql = "SELECT * FROM transactions WHERE account_number = '$accountNumber' 
              ORDER BY transaction_date DESC LIMIT 10";
$transactions = $conn->query($trans_sql);

// Handle messages
$message = '';
if (isset($_SESSION['message'])) {
    $message = $_SESSION['message'];
    unset($_SESSION['message']);
}
$error = '';
if (isset($_SESSION['error'])) {
    $error = $_SESSION['error'];
    unset($_SESSION['error']);
}
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, viewport-fit=cover">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta name="apple-mobile-web-app-status-bar-style" content="black-translucent">
    <meta name="apple-mobile-web-app-title" content="Simple Bank">
    <title>Dashboard - Simple Banking</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: #f0f2f5;
            transition: all 0.3s ease;
        }
        
        /* Dark Mode Styles */
        body.dark-mode {
            background: #1a202c;
        }
        
        body.dark-mode .header {
            background: linear-gradient(135deg, #4a5568 0%, #2d3748 100%);
        }
        
        body.dark-mode .balance-card,
        body.dark-mode .transactions,
        body.dark-mode .action-btn {
            background: #2d3748;
            color: white;
        }
        
        body.dark-mode .transaction-item {
            border-bottom-color: #4a5568;
        }
        
        body.dark-mode .balance-amount {
            color: #9f7aea;
        }
        
        .header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 20px;
            text-align: center;
        }
        
        .balance-card {
            background: white;
            border-radius: 15px;
            padding: 20px;
            margin: 20px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            text-align: center;
            transition: all 0.3s ease;
        }
        
        .balance-amount {
            font-size: 36px;
            font-weight: bold;
            color: #667eea;
        }
        
        .account-info {
            font-size: 12px;
            color: #666;
            margin-top: 5px;
        }
        
        body.dark-mode .account-info {
            color: #a0aec0;
        }
        
        .button-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
            gap: 15px;
            padding: 20px;
        }
        
        .action-btn {
            padding: 15px;
            border: none;
            border-radius: 10px;
            font-size: 14px;
            font-weight: bold;
            cursor: pointer;
            transition: transform 0.2s, opacity 0.2s;
            color: white;
        }
        
        .action-btn:hover {
            transform: scale(1.05);
            opacity: 0.9;
        }
        
        .transactions {
            background: white;
            border-radius: 15px;
            margin: 20px;
            padding: 20px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            transition: all 0.3s ease;
        }
        
        .transaction-item {
            border-bottom: 1px solid #e2e8f0;
            padding: 12px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 10px;
        }
        
        .transaction-date {
            font-size: 12px;
            color: #666;
            min-width: 100px;
        }
        
        .transaction-type {
            font-weight: bold;
            padding: 4px 8px;
            border-radius: 5px;
            font-size: 12px;
        }
        
        .type-deposit {
            background: #c6f6d5;
            color: #22543d;
        }
        
        .type-withdrawal {
            background: #fed7d7;
            color: #742a2a;
        }
        
        .type-transfer_sent {
            background: #feebc8;
            color: #7c2d12;
        }
        
        .type-transfer_received {
            background: #c6f6d5;
            color: #22543d;
        }
        
        .transaction-amount {
            font-weight: bold;
            min-width: 80px;
            text-align: right;
        }
        
        .transaction-desc {
            flex: 1;
            font-size: 13px;
            color: #666;
        }
        
        body.dark-mode .transaction-desc {
            color: #cbd5e0;
        }
        
        body.dark-mode .transaction-date {
            color: #a0aec0;
        }
        
        .message {
            background: #c6f6d5;
            color: #22543d;
            padding: 12px;
            margin: 20px;
            border-radius: 10px;
            text-align: center;
        }
        
        .error {
            background: #fed7d7;
            color: #742a2a;
        }
        
        h3 {
            margin-bottom: 15px;
        }
        
        .no-transactions {
            text-align: center;
            color: #999;
            padding: 20px;
        }
        
        /* Modal Styles */
        .modal {
            display: none;
            position: fixed;
            z-index: 1000;
            left: 0;
            top: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(0,0,0,0.5);
            justify-content: center;
            align-items: center;
        }
        
        .modal-content {
            background-color: white;
            margin: auto;
            padding: 25px;
            border-radius: 15px;
            width: 90%;
            max-width: 400px;
            position: relative;
            animation: modalopen 0.3s;
        }
        
        body.dark-mode .modal-content {
            background-color: #2d3748;
            color: white;
        }
        
        @keyframes modalopen {
            from { opacity: 0; transform: translateY(-50px); }
            to { opacity: 1; transform: translateY(0); }
        }
        
        .modal-content h3 {
            margin-bottom: 20px;
            text-align: center;
        }
        
        .modal-content input {
            width: 100%;
            padding: 12px;
            margin: 10px 0;
            border: 1px solid #ddd;
            border-radius: 8px;
            font-size: 14px;
        }
        
        body.dark-mode .modal-content input {
            background: #4a5568;
            border-color: #718096;
            color: white;
        }
        
        .modal-content button {
            width: 100%;
            padding: 12px;
            margin: 10px 0;
            border: none;
            border-radius: 8px;
            font-size: 16px;
            cursor: pointer;
            font-weight: bold;
        }
        
        .btn-confirm {
            background: #48bb78;
            color: white;
        }
        
        .btn-cancel {
            background: #718096;
            color: white;
        }
        
        .close {
            position: absolute;
            right: 20px;
            top: 10px;
            font-size: 28px;
            cursor: pointer;
        }
        
        /* Responsive */
        @media (max-width: 768px) {
            .button-grid {
                grid-template-columns: repeat(2, 1fr);
                gap: 10px;
                padding: 15px;
            }
            
            .action-btn {
                padding: 12px;
                font-size: 12px;
            }
            
            .balance-amount {
                font-size: 28px;
            }
            
            .transaction-item {
                flex-direction: column;
                align-items: flex-start;
            }
            
            .transaction-amount {
                text-align: left;
            }
        }
    </style>
</head>
<body>
    <div class="header">
        <h1>🏦 Simple Banking</h1>
        <p>Welcome, <?php echo htmlspecialchars($account['account_holder_name']); ?></p>
    </div>
    
    <?php if($message): ?>
        <div class="message"><?php echo htmlspecialchars($message); ?></div>
    <?php endif; ?>
    
    <?php if($error): ?>
        <div class="message error"><?php echo htmlspecialchars($error); ?></div>
    <?php endif; ?>
    
    <div class="balance-card">
        <h3>Current Balance</h3>
        <div class="balance-amount">$<?php echo number_format($account['balance'], 2); ?></div>
        <div class="account-info">Account: <?php echo $accountNumber; ?></div>
    </div>
    
    <div class="button-grid">
        <button class="action-btn" style="background: #48bb78;" onclick="openModal('deposit')">💰 DEPOSIT</button>
        <button class="action-btn" style="background: #f56565;" onclick="openModal('withdraw')">💸 WITHDRAW</button>
        <button class="action-btn" style="background: #ed8936;" onclick="openModal('transfer')">🔄 TRANSFER</button>
        <button class="action-btn" style="background: #4299e1;" onclick="location.href='history.php'">📜 HISTORY</button>
        <button class="action-btn" style="background: #9f7aea;" onclick="openModal('changepin')">🔐 CHANGE PIN</button>
        <button class="action-btn" style="background: #38b2ac;" onclick="location.href='goals.php'">🎯 SAVINGS GOALS</button>
        <button class="action-btn" style="background: #ed64a6;" onclick="location.href='budget.php'">📊 BUDGET</button>
        <button class="action-btn" style="background: #319795;" onclick="location.href='qr_payment.php'">📷 QR PAY</button>
        <button class="action-btn" style="background: #ecc94b; color:#333;" onclick="location.href='recurring_payments.php'">🔄 RECURRING</button>
        <button class="action-btn" style="background: #4a5568;" onclick="location.href='search_transactions.php'">🔍 SEARCH</button>
        <button class="action-btn" style="background: #2f855a;" onclick="location.href='export_excel.php'">📎 EXPORT</button>
        <button class="action-btn" style="background: #6b46c1;" onclick="location.href='statement.php'">📄 STATEMENT</button>
        <button class="action-btn" style="background: #744210; color:#fff;" onclick="toggleDarkMode()">🌙 DARK MODE</button>
        <button class="action-btn" style="background: #c53030;" onclick="location.href='logout.php'">🚪 LOGOUT</button>
    </div>
    
    <div class="transactions">
        <h3>📋 Recent Transactions</h3>
        <?php if($transactions->num_rows > 0): ?>
            <?php while($trans = $transactions->fetch_assoc()): 
                $typeClass = '';
                switch($trans['transaction_type']) {
                    case 'DEPOSIT': $typeClass = 'type-deposit'; break;
                    case 'WITHDRAWAL': $typeClass = 'type-withdrawal'; break;
                    case 'TRANSFER_SENT': $typeClass = 'type-transfer_sent'; break;
                    case 'TRANSFER_RECEIVED': $typeClass = 'type-transfer_received'; break;
                    default: $typeClass = '';
                }
            ?>
                <div class="transaction-item">
                    <div class="transaction-date"><?php echo date('M d, H:i', strtotime($trans['transaction_date'])); ?></div>
                    <div class="transaction-type <?php echo $typeClass; ?>"><?php echo $trans['transaction_type']; ?></div>
                    <div class="transaction-amount" style="color: <?php echo in_array($trans['transaction_type'], ['DEPOSIT', 'TRANSFER_RECEIVED']) ? '#48bb78' : '#f56565'; ?>;">
                        <?php echo in_array($trans['transaction_type'], ['DEPOSIT', 'TRANSFER_RECEIVED']) ? '+' : '-'; ?>$<?php echo number_format($trans['amount'], 2); ?>
                    </div>
                    <div class="transaction-desc"><?php echo htmlspecialchars($trans['description']); ?></div>
                </div>
            <?php endwhile; ?>
        <?php else: ?>
            <div class="no-transactions">No transactions yet. Make your first deposit!</div>
        <?php endif; ?>
    </div>
    
    <!-- Deposit Modal -->
    <div id="depositModal" class="modal">
        <div class="modal-content">
            <span class="close" onclick="closeModal('depositModal')">&times;</span>
            <h3>💰 Deposit Money</h3>
            <form action="process_transaction.php" method="post">
                <input type="number" name="amount" placeholder="Amount" step="0.01" required autocomplete="off">
                <input type="text" name="description" placeholder="Description (optional)">
                <input type="hidden" name="action" value="deposit">
                <button type="submit" class="btn-confirm">Confirm Deposit</button>
                <button type="button" class="btn-cancel" onclick="closeModal('depositModal')">Cancel</button>
            </form>
        </div>
    </div>
    
    <!-- Withdraw Modal -->
    <div id="withdrawModal" class="modal">
        <div class="modal-content">
            <span class="close" onclick="closeModal('withdrawModal')">&times;</span>
            <h3>💸 Withdraw Money</h3>
            <form action="process_transaction.php" method="post">
                <input type="number" name="amount" placeholder="Amount" step="0.01" required autocomplete="off">
                <input type="text" name="description" placeholder="Description (optional)">
                <input type="hidden" name="action" value="withdraw">
                <button type="submit" class="btn-confirm">Confirm Withdrawal</button>
                <button type="button" class="btn-cancel" onclick="closeModal('withdrawModal')">Cancel</button>
            </form>
        </div>
    </div>
    
    <!-- Transfer Modal -->
    <div id="transferModal" class="modal">
        <div class="modal-content">
            <span class="close" onclick="closeModal('transferModal')">&times;</span>
            <h3>🔄 Transfer Money</h3>
            <form action="process_transaction.php" method="post">
                <input type="text" name="toAccount" placeholder="Recipient Account Number" required autocomplete="off">
                <input type="number" name="amount" placeholder="Amount" step="0.01" required autocomplete="off">
                <input type="password" name="pin" placeholder="Your PIN" maxlength="4" required autocomplete="off">
                <input type="hidden" name="action" value="transfer">
                <button type="submit" class="btn-confirm">Confirm Transfer</button>
                <button type="button" class="btn-cancel" onclick="closeModal('transferModal')">Cancel</button>
            </form>
        </div>
    </div>
    
    <!-- Change PIN Modal -->
    <div id="changepinModal" class="modal">
        <div class="modal-content">
            <span class="close" onclick="closeModal('changepinModal')">&times;</span>
            <h3>🔐 Change PIN</h3>
            <form action="process_transaction.php" method="post">
                <input type="password" name="oldPin" placeholder="Current PIN" maxlength="4" required autocomplete="off">
                <input type="password" name="newPin" placeholder="New PIN (4 digits)" maxlength="4" required autocomplete="off">
                <input type="hidden" name="action" value="changepin">
                <button type="submit" class="btn-confirm">Change PIN</button>
                <button type="button" class="btn-cancel" onclick="closeModal('changepinModal')">Cancel</button>
            </form>
        </div>
    </div>
    
    <script>
        // Modal functions
        function openModal(type) {
            document.getElementById(type + 'Modal').style.display = 'flex';
        }
        
        function closeModal(id) {
            document.getElementById(id).style.display = 'none';
        }
        
        // Close modal when clicking outside
        window.onclick = function(event) {
            if (event.target.classList.contains('modal')) {
                event.target.style.display = 'none';
            }
        }
        
        // Dark Mode Toggle
        function toggleDarkMode() {
            document.body.classList.toggle('dark-mode');
            localStorage.setItem('darkMode', document.body.classList.contains('dark-mode'));
        }
        
        // Load dark mode preference
        if (localStorage.getItem('darkMode') === 'true') {
            document.body.classList.add('dark-mode');
        }
        
        // Auto-hide messages after 5 seconds
        setTimeout(function() {
            let messages = document.querySelectorAll('.message');
            messages.forEach(function(msg) {
                msg.style.display = 'none';
            });
        }, 5000);
    </script>
</body>
</html>