package io.perfana.tinybank.domain;

public record AccountInfo (Account account, Balance balance, Transaction lastTransaction) { }
