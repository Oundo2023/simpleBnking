<!DOCTYPE html>
<html>
<head>
    <title>Create Account - Simple Banking</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
        }
        
        .container {
            background: white;
            border-radius: 20px;
            box-shadow: 0 20px 60px rgba(0,0,0,0.3);
            padding: 40px;
            width: 400px;
        }
        
        h1 {
            color: #667eea;
            margin-bottom: 10px;
            text-align: center;
        }
        
        .subtitle {
            color: #666;
            margin-bottom: 30px;
            text-align: center;
            font-size: 14px;
        }
        
        input {
            width: 100%;
            padding: 12px;
            margin: 10px 0;
            border: 1px solid #ddd;
            border-radius: 8px;
            font-size: 16px;
        }
        
        button {
            width: 100%;
            padding: 12px;
            margin: 10px 0;
            background: #48bb78;
            color: white;
            border: none;
            border-radius: 8px;
            font-size: 16px;
            cursor: pointer;
        }
        
        .back-btn {
            background: #718096;
        }
        
        .error {
            background: #fed7d7;
            color: #742a2a;
            padding: 10px;
            margin: 10px 0;
            border-radius: 8px;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>📝 Create Account</h1>
        <div class="subtitle">Join Simple Banking Today</div>
        
        <?php if(isset($_GET['error'])): ?>
            <div class="error">❌ Account number already exists!</div>
        <?php endif; ?>
        
        <form action="register_process.php" method="post">
            <input type="text" name="accountNumber" placeholder="Account Number" required>
            <input type="text" name="name" placeholder="Full Name" required>
            <input type="password" name="pin" placeholder="4-digit PIN" maxlength="4" required>
            <input type="number" name="deposit" placeholder="Initial Deposit" step="0.01" required>
            <button type="submit">✅ CREATE ACCOUNT</button>
        </form>
        
        <button class="back-btn" onclick="location.href='index.php'">← BACK TO LOGIN</button>
    </div>
</body>
</html>