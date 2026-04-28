<!DOCTYPE html>
<html>
<head>
    <title>Simple Banking System</title>
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
            text-align: center;
        }
        
        h1 {
            color: #667eea;
            margin-bottom: 10px;
            font-size: 28px;
        }
        
        .subtitle {
            color: #666;
            margin-bottom: 30px;
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
            background: #667eea;
            color: white;
            border: none;
            border-radius: 8px;
            font-size: 16px;
            cursor: pointer;
            transition: transform 0.2s;
        }
        
        button:hover {
            transform: scale(1.02);
        }
        
        .register-btn {
            background: #48bb78;
        }
        
        .message {
            padding: 10px;
            margin: 10px 0;
            border-radius: 8px;
        }
        
        .error {
            background: #fed7d7;
            color: #742a2a;
        }
        
        .success {
            background: #c6f6d5;
            color: #22543d;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>🏦 Simple Banking</h1>
        <div class="subtitle">Secure Mobile Banking</div>
        
        <?php if(isset($_GET['error'])): ?>
            <div class="message error">❌ Invalid account number or PIN!</div>
        <?php endif; ?>
        
        <?php if(isset($_GET['success'])): ?>
            <div class="message success">✅ Account created successfully! Please login.</div>
        <?php endif; ?>
        
        <form action="login.php" method="post">
            <input type="text" name="accountNumber" placeholder="Account Number" required>
            <input type="password" name="pin" placeholder="PIN" maxlength="4" required>
            <button type="submit">🔐 LOGIN</button>
        </form>
        
        <button class="register-btn" onclick="location.href='register.php'">📝 CREATE NEW ACCOUNT</button>
    </div>
</body>
</html>