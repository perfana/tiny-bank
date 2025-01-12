package io.perfana.tinybank.domain;

public record Account(Customer customer, String accountNumber, String name) { }
