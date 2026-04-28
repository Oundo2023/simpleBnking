<?php
include 'db_connection.php';

$accountNumber = $_POST['accountNumber'];
$name = $_POST['name'];
$pin = $_POST['pin'];
$deposit = $_POST['deposit'];

// Check if account exists
$check = "SELECT * FROM accounts WHERE account_number = '$accountNumber'";
$result = $conn->query($check);

if ($result->num_rows > 0) {
    header("Location: register.php?error=1");
} else {
    // Create account
    $sql = "INSERT INTO accounts (account_number, account_holder_name, pin, balance) 
            VALUES ('$accountNumber', '$name', '$pin', $deposit)";
    
    if ($conn->query($sql) === TRUE) {
        // Add initial deposit transaction
        $trans = "INSERT INTO transactions (account_number, transaction_type, amount, description) 
                  VALUES ('$accountNumber', 'DEPOSIT', $deposit, 'Initial deposit')";
        $conn->query($trans);
        header("Location: index.php?success=1");
    } else {
        header("Location: register.php?error=1");
    }
}

$conn->close();
?>