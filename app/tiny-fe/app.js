import fetch from 'node-fetch';
import express from 'express';
import bodyParser from 'body-parser';
import messages from './messages.js';

const app = express();
app.use(express.static('public'));

app.use(bodyParser.urlencoded({ extended: false }));
app.get('/', (req, res) => {
    res.send(messages.home);
});
app.get('/about', (req, res) => {
    res.send(messages.about);
});
app.post('/submit', (req, res, next) => {
    const uid = req.body.uid;

    if (!uid) {
        const err = new Error('User id is required');
        return next(err);
    }

    console.log(`User id submitted: ${uid}`);

    res.send(`
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Loading...</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 0;
            padding: 0;
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
            background-color: #f4f4f4;
        }
        .spinner {
            display: flex;
            flex-direction: column;
            align-items: center;
        }
        .spinner .circle {
            width: 50px;
            height: 50px;
            border: 5px solid #f3f3f3;
            border-top: 5px solid #3498db;
            border-radius: 50%;
            animation: spin 1s linear infinite;
        }
        .spinner .timer {
            margin-top: 10px;
            font-size: 1.2em;
        }
        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }
    </style>
</head>
<body>
    <div class="spinner">
        <div class="circle"></div>
        <div class="timer" id="timer">0.0s</div>
    </div>
    <script>
        let timerInterval;
        let seconds = 0;
        timerInterval = setInterval(() => {
            seconds += 0.1;
            document.getElementById('timer').textContent = seconds.toFixed(1) + 's';
        }, 100);
        setTimeout(function() {
            window.location.href = '/fetchData?uid=${uid}';
        }, 10); // Redirect after 10 milliseconds
        setTimeout(function() {
            clearInterval(timerInterval);
        }, 10000); // Stop counter after 10 sec
    </script>
</body>
</html>
    `);
});

app.get('/fetchData', (req, res, next) => {
    const uid = req.query.uid;

    if (!uid) {
        const err = new Error('User id is required');
        return next(err);
    }

    fetch('http://localhost:18080/accountInfo?userId=' + uid)
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(accountData => {
            res.send(`
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Account Information</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 0;
            padding: 0;
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
            background-color: #f4f4f4;
        }
        .container {
            background-color: #fff;
            padding: 20px;
            margin: 20px;
            border-radius: 8px;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
            width: 90%;
            max-width: 700px;
        }
        h1 {
            text-align: center;
        }
        .account-info, .balance-info, .transaction-info {
            margin-bottom: 20px;
        }
        .account-info h3, .balance-info h3, .transaction-info h3 {
            border-bottom: 1px solid #ccc;
            padding-bottom: 5px;
        }
        .info {
            display: flex;
            justify-content: space-between;
            margin: 10px 0;
        }
        .info label {
            font-weight: bold;
            flex: 1;
        }
        .info span {
            flex: 2;
            text-align: right;
        }
        .spinner {
            display: flex;
            flex-direction: column;
            align-items: center;
        }
        .spinner .circle {
            width: 50px;
            height: 50px;
            border: 5px solid #f3f3f3;
            border-top: 5px solid #3498db;
            border-radius: 50%;
            animation: spin 1s linear infinite;
        }
        .spinner .timer {
            margin-top: 10px;
            font-size: 1.2em;
        }
        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }
    </style>
</head>
<body>
    <div class="spinner" id="spinner">
        <div class="circle"></div>
        <div class="timer" id="timer">0.0s</div>
    </div>
    <div class="container" id="content" style="display: none;">
        <h1>Account Overview</h1>
        <div class="account-info">
            <h3>Account</h3>
            <div class="info">
                <label>Customer Name:</label> <span>${accountData.account.customer.name}</span>
            </div>
            <div class="info">
                <label>Account Number:</label> <span>${accountData.account.accountNumber}</span>
            </div>
            <div class="info">
                <label>Account Name:</label> <span>${accountData.account.name}</span>
            </div>
        </div>
        <div class="balance-info">
            <h3>Balance</h3>
            <div class="info">
                <label>Amount:</label> <span>${accountData.balance.amount} ${accountData.balance.currency}</span>
            </div>
        </div>
        <div class="transaction-info">
            <h3>Previous Transaction</h3>
            <div class="info">
                <label>From Account:</label> <span>${accountData.lastTransaction.fromAccount}</span>
            </div>
            <div class="info">
                <label>To Account:</label> <span>${accountData.lastTransaction.toAccount}</span>
            </div>
            <div class="info">
                <label>Amount:</label> <span>${accountData.lastTransaction.amount} ${accountData.lastTransaction.currency}</span>
            </div>
            <div class="info">
                <label>Description:</label> <span>${accountData.lastTransaction.description}</span>
            </div>
            <div class="info">
                <label>Transaction Date:</label> <span>${accountData.lastTransaction.transactionDate}</span>
            </div>
        </div>
        <button onclick="refreshAccountInfo()">Refresh</button>
        <button onclick="window.location.href='/'">Home</button>
    </div>
    <script>
        let timerInterval;
        function refreshAccountInfo() {
            document.getElementById('spinner').style.display = 'flex';
            document.getElementById('content').style.display = 'none';
            let seconds = 0;
            timerInterval = setInterval(() => {
                seconds += 0.1;
                document.getElementById('timer').textContent = seconds.toFixed(1) + 's';
            }, 100);
            window.location.href = '/fetchData?uid=${uid}';
        }
        window.onload = function() {
            document.getElementById('spinner').style.display = 'none';
            document.getElementById('content').style.display = 'block';
            clearInterval(timerInterval);
        };
    </script>
</body>
</html>
            `);
        })
        .catch(error => {
            console.error('There was a problem with the fetch operation:', error);
            res.status(500).send(`
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Error</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            background-color: #f8d7da;
            margin: 0;
        }
        .container {
            background-color: #ffffff;
            padding: 20px;
            border: 2px solid #f5c6cb;
            border-radius: 8px;
            text-align: center;
            width: 80%;
            max-width: 600px;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
        }
        .exclamation-mark {
            font-size: 100px;
            color: #721c24;
        }
        .error-message {
            color: #721c24;
            font-size: 1.2em;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="exclamation-mark">❗</div>
        <div class="error-message">
            <h1>Error fetching account information</h1>
            <p>${error.message}</p>
            <button onclick="window.location.href='/'">Home</button>
        </div>
    </div>
</body>
</html>
            `);
        });
});

app.post('/transactions', (req, res, next) => {
    const uid = req.body.uid;

    if (!uid) {
        const err = new Error('User id is required');
        return next(err);
    }

    res.send(`
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Loading...</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 0;
            padding: 0;
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
            background-color: #f4f4f4;
        }
        .spinner {
            display: flex;
            flex-direction: column;
            align-items: center;
        }
        .spinner .circle {
            width: 50px;
            height: 50px;
            border: 5px solid #f3f3f3;
            border-top: 5px solid #3498db;
            border-radius: 50%;
            animation: spin 1s linear infinite;
        }
        .spinner .timer {
            margin-top: 10px;
            font-size: 1.2em;
        }
        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }
    </style>
</head>
<body>
    <div class="spinner">
        <div class="circle"></div>
        <div class="timer" id="timer">0.0s</div>
    </div>
    <script>
        let timerInterval;
        let seconds = 0;
        timerInterval = setInterval(() => {
            seconds += 0.1;
            document.getElementById('timer').textContent = seconds.toFixed(1) + 's';
        }, 100);
        setTimeout(function() {
            window.location.href = '/fetchTransactions?uid=${uid}';
        }, 10); // Redirect after 10 milliseconds
        setTimeout(function() {
            clearInterval(timerInterval);
        }, 10000); // Stop counter after 10 sec
    </script>
</body>
</html>
    `);
});

app.get('/fetchTransactions', (req, res, next) => {
    const uid = req.query.uid;

    if (!uid) {
        const err = new Error('User id is required');
        return next(err);
    }

    fetch('http://localhost:18080/transactions?userId=' + uid)
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(transactionData => {
            res.send(`
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Transaction Information</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 0;
            padding: 0;
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
            background-color: #f4f4f4;
        }
        .container {
            background-color: #fff;
            padding: 20px;
            margin: 20px;
            border-radius: 8px;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
            width: 90%;
            max-width: 700px;
        }
        h1 {
            text-align: center;
        }
        .account-info, .transaction-info {
            margin-bottom: 20px;
        }
        .account-info h3, .transaction-info h3 {
            border-bottom: 1px solid #ccc;
            padding-bottom: 5px;
        }
        .info {
            display: flex;
            justify-content: space-between;
            margin: 10px 0;
        }
        .info label {
            font-weight: bold;
            flex: 1;
        }
        .info span {
            flex: 2;
            text-align: right;
        }
        .spinner {
            display: flex;
            flex-direction: column;
            align-items: center;
        }
        .spinner .circle {
            width: 50px;
            height: 50px;
            border: 5px solid #f3f3f3;
            border-top: 5px solid #3498db;
            border-radius: 50%;
            animation: spin 1s linear infinite;
        }
        .spinner .timer {
            margin-top: 10px;
            font-size: 1.2em;
        }
        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }
        table {
            width: 100%;
            border-collapse: collapse;
            font-size: 0.8em; /* Smaller font size */
        }
        th, td {
            padding: 8px 12px;
            border: 1px solid #ccc;
            text-align: left;
        }
        th {
            background-color: #f4f4f4;
        }
    </style>
</head>
<body>
    <div class="spinner" id="spinner">
        <div class="circle"></div>
        <div class="timer" id="timer">0.0s</div>
    </div>
    <div class="container" id="content" style="display: none;">
        <h1>Transactions</h1>
        <div class="account-info">
            <h3>Account</h3>
            <div class="info">
                <label>Customer Name:</label> <span>${transactionData.account.customer.name}</span>
            </div>
            <div class="info">
                <label>Account Number:</label> <span>${transactionData.account.accountNumber}</span>
            </div>
            <div class="info">
                <label>Account Name:</label> <span>${transactionData.account.name}</span>
            </div>
        </div>
        <div class="transaction-info">
            <h3>Transactions</h3>
            <table>
                <thead>
                    <tr>
                        <th>Date</th>
                        <th>From</th>
                        <th>To</th>
                        <th>Amount</th>
                        <th>Currency</th>
                        <th>Description</th>
                    </tr>
                </thead>
                <tbody>
                    ${transactionData.transactions.map(transaction => `
                    <tr>
                        <td>${transaction.transactionDate}</td>
                        <td>${transaction.fromAccount}</td>
                        <td>${transaction.toAccount}</td>
                        <td>${transaction.amount}</td>
                        <td>${transaction.currency}</td>
                        <td>${transaction.description}</td>
                    </tr>
                    `).join('')}
                </tbody>
            </table>
        </div>
        <button onclick="refreshTransactionInfo()">Refresh</button>
        <button onclick="window.location.href='/'">Home</button>
    </div>
    <script>
        let timerInterval;
        function refreshTransactionInfo() {
            document.getElementById('spinner').style.display = 'flex';
            document.getElementById('content').style.display = 'none';
            let seconds = 0;
            timerInterval = setInterval(() => {
                seconds += 0.1;
                document.getElementById('timer').textContent = seconds.toFixed(1) + 's';
            }, 100);
            window.location.href = '/fetchTransactions?uid=${uid}';
        }
        document.addEventListener('DOMContentLoaded', function() {
            document.getElementById('spinner').style.display = 'none';
            document.getElementById('content').style.display = 'block';
            clearInterval(timerInterval);
        });
    </script>
</body>
</html>
            `);
        })
        .catch(error => {
            console.error('There was a problem with the fetch operation:', error);
            res.status(500).send(`
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Error</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            background-color: #f4f4f4;
            margin: 0;
        }
        .error-container {
            background-color: #fff;
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
            text-align: center;
            width: 90%;
            max-width: 400px;
            border-left: 5px solid red;
        }
        .error-icon {
            font-size: 50px;
            color: red;
        }
        .error-message {
            margin: 20px 0;
            font-size: 1.2em;
        }
    </style>
</head>
<body>
    <div class="error-container">
        <div class="error-icon">❗</div>
        <div class="error-message">Error fetching transaction information: ${error.message}</div>
        <button onclick="window.location.href='/'">Home</button>
    </div>
</body>
</html>
`);
        });
});

const port = 13000;
app.listen(port, () => {
    console.log(`Server is running at http://localhost:${port}`);
});