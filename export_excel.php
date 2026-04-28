<?php
session_start();
include 'db_connection.php';

if (!isset($_SESSION['accountNumber'])) {
    header("Location: index.php");
    exit();
}

$accountNumber = $_SESSION['accountNumber'];

// Get all transactions
$transactions = $conn->query("SELECT * FROM transactions WHERE account_number = '$accountNumber' ORDER BY transaction_date DESC");

// Set headers for Excel download
header('Content-Type: text/csv');
header('Content-Disposition: attachment; filename="transactions_' . date('Y-m-d') . '.csv"');

// Create output stream
$output = fopen('php://output', 'w');

// Add headers
fputcsv($output, ['Date', 'Type', 'Amount', 'Description', 'Related Account']);

// Add data
while($row = $transactions->fetch_assoc()) {
    fputcsv($output, [
        $row['transaction_date'],
        $row['transaction_type'],
        $row['amount'],
        $row['description'],
        $row['related_account']
    ]);
}

fclose($output);
exit();
?>