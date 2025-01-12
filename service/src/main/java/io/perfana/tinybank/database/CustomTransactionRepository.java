package io.perfana.tinybank.database;

import io.perfana.tinybank.domain.Transaction;

import java.util.List;

public interface CustomTransactionRepository {
    Transaction findLastTransaction(String accountNumber);
    List<Transaction> findTransactions(String accountNumber);
}
