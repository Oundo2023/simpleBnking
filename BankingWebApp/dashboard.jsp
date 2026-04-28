<%@ page import="java.util.*, java.text.*" %>
<%@ page import="DatabaseConnection" %>
<%
    String accountNumber = (String) session.getAttribute("accountNumber");
    if (accountNumber == null) {
        response.sendRedirect("index.html");
        return;
    }
    
    DatabaseConnection db = new DatabaseConnection();
    HashMap<String, Object> account = db.getAccountDetails(accountNumber);
    double balance = (Double) account.get("balance");
    String holderName = (String) account.get("account_holder_name");
    
    ArrayList<HashMap<String, String>> transactions = db.getTransactionHistory(accountNumber, 10);
    db.close();
    
    String message = (String) session.getAttribute("message");
    String error = (String) session.getAttribute("error");
    session.removeAttribute("message");
    session.removeAttribute("error");
%>

<!DOCTYPE html>
<html>
<head>
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
        }
        
        .balance-amount {
            font-size: 36px;
            font-weight: bold;
            color: #667eea;
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
            font-size: 16px;
            cursor: pointer;
            transition: transform 0.2s;
            color: white;
        }
        
        .action-btn:hover {
            transform: scale(1.05);
        }
        
        .deposit { background: #48bb78; }
        .withdraw { background: #f56565; }
        .transfer { background: #ed8936; }
        .history { background: #4299e1; }
        .change-pin { background: #9f7aea; }
        .logout { background: #718096; }
        
        .transactions {
            background: white;
            border-radius: 15px;
            margin: 20px;
            padding: 20px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        
        .transaction-item {
            border-bottom: 1px solid #e2e8f0;
            padding: 10px;
            display: flex;
            justify-content: space-between;
        }
        
        .modal {
            display: none;
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0,0,0,0.5);
            justify-content: center;
            align-items: center;
        }
        
        .modal-content {
            background: white;
            padding: 30px;
            border-radius: 15px;
            width: 350px;
        }
        
        .modal-content input, .modal-content select {
            width: 100%;
            padding: 10px;
            margin: 10px 0;
            border: 1px solid #ddd;
            border-radius: 5px;
        }
        
        .close-btn {
            background: #718096;
            margin-top: 10px;
        }
        
        .message {
            background: #c6f6d5;
            color: #22543d;
            padding: 10px;
            margin: 20px;
            border-radius: 10px;
            text-align: center;
        }
        
        .error {
            background: #fed7d7;
            color: #742a2a;
        }
        
        h2, h3 {
            margin-bottom: 15px;
        }
    </style>
</head>
<body>
    <div class="header">
        <h1>🏦 Simple Banking</h1>
        <p>Welcome, <%= holderName %></p>
    </div>
    
    <% if (message != null) { %>
        <div class="message"><%= message %></div>
    <% } %>
    
    <% if (error != null) { %>
        <div class="message error"><%= error %></div>
    <% } %>
    
    <div class="balance-card">
        <h3>Current Balance</h3>
        <div class="balance-amount">$<%= String.format("%,.2f", balance) %></div>
        <p>Account: <%= accountNumber %></p>
    </div>
    
    <div class="button-grid">
        <button class="action-btn deposit" onclick="openModal('deposit')">💰 DEPOSIT</button>
        <button class="action-btn withdraw" onclick="openModal('withdraw')">💸 WITHDRAW</button>
        <button class="action-btn transfer" onclick="openModal('transfer')">🔄 TRANSFER</button>
        <button class="action-btn history" onclick="location.href='history.jsp'">📜 HISTORY</button>
        <button class="action-btn change-pin" onclick="openModal('changepin')">🔐 CHANGE PIN</button>
        <button class="action-btn logout" onclick="location.href='TransactionServlet?action=logout'">🚪 LOGOUT</button>
    </div>
    
    <div class="transactions">
        <h3>Recent Transactions</h3>
        <% for (HashMap<String, String> trans : transactions) { %>
            <div class="transaction-item">
                <span><%= trans.get("date").substring(0,16) %></span>
                <span><%= trans.get("type") %></span>
                <span>$<%= trans.get("amount") %></span>
                <span><%= trans.get("description") %></span>
            </div>
        <% } %>
        <% if (transactions.isEmpty()) { %>
            <p style="text-align:center; color:#999;">No transactions yet</p>
        <% } %>
    </div>
    
    <!-- Deposit Modal -->
    <div id="depositModal" class="modal">
        <div class="modal-content">
            <h3>Deposit Money</h3>
            <form action="TransactionServlet" method="post">
                <input type="number" name="amount" placeholder="Amount" step="0.01" required>
                <input type="text" name="description" placeholder="Description (optional)">
                <input type="hidden" name="action" value="deposit">
                <button type="submit" style="background:#48bb78;">Confirm Deposit</button>
                <button type="button" class="close-btn" onclick="closeModal('depositModal')">Cancel</button>
            </form>
        </div>
    </div>
    
    <!-- Withdraw Modal -->
    <div id="withdrawModal" class="modal">
        <div class="modal-content">
            <h3>Withdraw Money</h3>
            <form action="TransactionServlet" method="post">
                <input type="number" name="amount" placeholder="Amount" step="0.01" required>
                <input type="text" name="description" placeholder="Description (optional)">
                <input type="hidden" name="action" value="withdraw">
                <button type="submit" style="background:#f56565;">Confirm Withdrawal</button>
                <button type="button" class="close-btn" onclick="closeModal('withdrawModal')">Cancel</button>
            </form>
        </div>
    </div>
    
    <!-- Transfer Modal -->
    <div id="transferModal" class="modal">
        <div class="modal-content">
            <h3>Transfer Money</h3>
            <form action="TransactionServlet" method="post">
                <input type="text" name="toAccount" placeholder="Recipient Account Number" required>
                <input type="number" name="amount" placeholder="Amount" step="0.01" required>
                <input type="password" name="pin" placeholder="Your PIN" maxlength="4" required>
                <input type="hidden" name="action" value="transfer">
                <button type="submit" style="background:#ed8936;">Confirm Transfer</button>
                <button type="button" class="close-btn" onclick="closeModal('transferModal')">Cancel</button>
            </form>
        </div>
    </div>
    
    <!-- Change PIN Modal -->
    <div id="changepinModal" class="modal">
        <div class="modal-content">
            <h3>Change PIN</h3>
            <form action="TransactionServlet" method="post">
                <input type="password" name="oldPin" placeholder="Current PIN" maxlength="4" required>
                <input type="password" name="newPin" placeholder="New PIN" maxlength="4" required>
                <input type="hidden" name="action" value="changePin">
                <button type="submit" style="background:#9f7aea;">Change PIN</button>
                <button type="button" class="close-btn" onclick="closeModal('changepinModal')">Cancel</button>
            </form>
        </div>
    </div>
    
    <script>
        function openModal(type) {
            document.getElementById(type + 'Modal').style.display = 'flex';
        }
        
        function closeModal(id) {
            document.getElementById(id).style.display = 'none';
        }
        
        window.onclick = function(event) {
            if (event.target.className === 'modal') {
                event.target.style.display = 'none';
            }
        }
    </script>
</body>
</html>