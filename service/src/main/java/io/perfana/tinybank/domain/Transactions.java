package io.perfana.tinybank.domain;

import java.util.List;

public record Transactions(Account account, List<Transaction> transactions) { }
