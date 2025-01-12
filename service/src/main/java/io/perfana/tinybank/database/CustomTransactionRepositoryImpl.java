package io.perfana.tinybank.database;

import io.perfana.tinybank.domain.Transaction;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class CustomTransactionRepositoryImpl implements CustomTransactionRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Transaction findLastTransaction(String accountNumber) {
        String jpql = "SELECT t FROM Transaction t WHERE t.toAccount = :accountNumber or t.fromAccount = :accountNumber ORDER BY t.transactionDate DESC";
        TypedQuery<Transaction> query = entityManager.createQuery(jpql, Transaction.class);
        query.setParameter("accountNumber", accountNumber);
        query.setMaxResults(1);
        return query.getResultList().stream().findFirst().orElse(null);
    }

    @Override
    public List<Transaction> findTransactions(String accountNumber) {
        String jpql = "SELECT t FROM Transaction t WHERE t.toAccount = :accountNumber or t.fromAccount = :accountNumber ORDER BY t.transactionDate DESC";
        TypedQuery<Transaction> query = entityManager.createQuery(jpql, Transaction.class);
        query.setParameter("accountNumber", accountNumber);
        return query.getResultList();
    }
}