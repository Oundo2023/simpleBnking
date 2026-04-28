<?php
session_start();
include 'db_connection.php';

$accountNumber = $_POST['accountNumber'];
$pin = $_POST['pin'];

$sql = "SELECT * FROM accounts WHERE account_number = '$accountNumber' AND pin = '$pin' AND is_active = TRUE";
$result = $conn->query($sql);

if ($result->num_rows > 0) {
    $_SESSION['accountNumber'] = $accountNumber;
    header("Location: dashboard.php");
} else {
    header("Location: index.php?error=1");
}

$conn->close();
?>