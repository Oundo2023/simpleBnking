<?php
session_start();
include 'db_connection.php';

if (!isset($_SESSION['accountNumber'])) {
    header("Location: index.php");
    exit();
}

$accountNumber = $_SESSION['accountNumber'];
$searchResults = [];
$searchPerformed = false;

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $searchPerformed = true;
    $searchType = $_POST['search_type'];
    $keyword = $_POST['keyword'];
    
    if ($searchType == 'amount') {
        $min = $_POST['min_amount'];
        $max = $_POST['max_amount'];
        $sql = "SELECT * FROM transactions WHERE account_number = '$accountNumber' AND amount BETWEEN $min AND $max ORDER BY transaction_date DESC";
    } elseif ($searchType == 'date') {
        $from = $_POST['from_date'];
        $to = $_POST['to_date'];
        $sql = "SELECT * FROM transactions WHERE account_number = '$accountNumber' AND DATE(transaction_date) BETWEEN '$from' AND '$to' ORDER BY transaction_date DESC";
    } else {
        $sql = "SELECT * FROM transactions WHERE account_number = '$accountNumber' AND (description LIKE '%$keyword%' OR transaction_type LIKE '%$keyword%') ORDER BY transaction_date DESC";
    }
    
    $searchResults = $conn->query($sql);
}
?>

<!DOCTYPE html>
<html>
<head>
    <title>Search Transactions - Simple Banking</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #f0f2f5; padding: 20px; }
        .container { max-width: 1000px; margin: 0 auto; }
        .search-card { background: white; border-radius: 15px; padding: 20px; margin-bottom: 20px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        .result-card { background: white; border-radius: 15px; padding: 20px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        input, select { padding: 10px; margin: 5px; border: 1px solid #ddd; border-radius: 8px; }
        button { background: #667eea; color: white; padding: 10px 20px; border: none; border-radius: 8px; cursor: pointer; }
        .btn-back { background: #718096; margin-bottom: 20px; }
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #e2e8f0; }
        th { background: #667eea; color: white; }
        .search-option { display: none; }
        .active { display: block; }
        h2 { margin-bottom: 20px; }
    </style>
    <script>
        function showSearch(type) {
            document.getElementById('amount_search').style.display = 'none';
            document.getElementById('date_search').style.display = 'none';
            document.getElementById('keyword_search').style.display = 'none';
            document.getElementById(type + '_search').style.display = 'block';
        }
    </script>
</head>
<body>
    <div class="container">
        <button class="btn-back" onclick="location.href='dashboard.php'">← Back to Dashboard</button>
        
        <div class="search-card">
            <h2>🔍 Search Transactions</h2>
            <div style="margin-bottom: 20px;">
                <button type="button" onclick="showSearch('amount')">💰 By Amount</button>
                <button type="button" onclick="showSearch('date')">📅 By Date</button>
                <button type="button" onclick="showSearch('keyword')">🔤 By Keyword</button>
            </div>
            
            <form method="POST">
                <div id="amount_search" class="search-option">
                    <h3>Search by Amount Range</h3>
                    <input type="hidden" name="search_type" value="amount">
                    <input type="number" name="min_amount" placeholder="Min Amount" step="0.01" required>
                    <input type="number" name="max_amount" placeholder="Max Amount" step="0.01" required>
                    <button type="submit">Search</button>
                </div>
                
                <div id="date_search" class="search-option">
                    <h3>Search by Date Range</h3>
                    <input type="hidden" name="search_type" value="date">
                    <input type="date" name="from_date" required>
                    <input type="date" name="to_date" required>
                    <button type="submit">Search</button>
                </div>
                
                <div id="keyword_search" class="search-option">
                    <h3>Search by Keyword</h3>
                    <input type="hidden" name="search_type" value="keyword">
                    <input type="text" name="keyword" placeholder="Enter keyword (deposit, withdrawal, description...)" size="40" required>
                    <button type="submit">Search</button>
                </div>
            </form>
        </div>
        
        <?php if($searchPerformed): ?>
            <div class="result-card">
                <h2>📋 Search Results</h2>
                <?php if($searchResults->num_rows > 0): ?>
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
                            <?php while($trans = $searchResults->fetch_assoc()): ?>
                                <tr>
                                    <td><?php echo $trans['transaction_date']; ?></td>
                                    <td><?php echo $trans['transaction_type']; ?></td>
                                    <td>$<?php echo number_format($trans['amount'], 2); ?></td>
                                    <td><?php echo $trans['description']; ?></td>
                                </tr>
                            <?php endwhile; ?>
                        </tbody>
                    </table>
                <?php else: ?>
                    <p style="text-align:center; color:#999;">No transactions found matching your search.</p>
                <?php endif; ?>
            </div>
        <?php endif; ?>
        
        <script>
            document.getElementById('amount_search').style.display = 'block';
        </script>
    </div>
</body>
</html>