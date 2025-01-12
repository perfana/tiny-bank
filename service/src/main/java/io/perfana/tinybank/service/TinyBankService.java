package io.perfana.tinybank.service;

import io.perfana.tinybank.database.TransactionRepository;
import io.perfana.tinybank.domain.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TinyBankService {

    @Autowired
    private AccountService accountService;

    @Autowired
    private BalanceService balanceService;

    @Autowired
    private TransactionRepository transactionRepository;

    @PostConstruct
    public void init() {
        Transaction transaction1 = new Transaction(
                1L,
                "LT121000011234567890",
                "DE50785612345678901234",
                100L,
                "EUR",
                "toys 🧸",
                LocalDate.parse("2024-08-25")
        );

        Transaction transaction2 = new Transaction(
                2L,
                "NL91ABNA0417164300",
                "LT121000011234567890",
                100L,
                "EUR",
                "books 📚",
                LocalDate.parse("2024-08-26")
        );

        Transaction transaction3 = new Transaction(
                3L,
                "US12BOFA0000123456",
                "LT121000011234567890",
                100L,
                "EUR",
                "music 🎵",
                LocalDate.parse("2024-08-27")
        );

        transactionRepository.save(transaction1);
        transactionRepository.save(transaction2);
        transactionRepository.save(transaction3);
    }

    public AccountInfo retrieveAccountInfo(String userId) {
        Account account = accountService.getAccount(userId);
        Balance balance = balanceService.getBalance(account.accountNumber());
        Transaction lastTransaction = transactionRepository.findLastTransaction(account.accountNumber());
        return new AccountInfo(account, balance, lastTransaction);
    }

    public Transactions retrieveTransactions(String userId) {
        Account account = accountService.getAccount(userId);
        List<Transaction> transactions = transactionRepository.findTransactions(account.accountNumber());
        return new Transactions(account, transactions);
    }
}
