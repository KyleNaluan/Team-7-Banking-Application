package com.team7.bankingapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@MappedSuperclass
@Getter
@Setter
public abstract class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "txn_seq")
    @SequenceGenerator(name = "txn_seq", sequenceName = "transaction_id_seq", allocationSize = 1)
    protected int transactionID;

    @Column(nullable = false)
    protected BigDecimal amount;

    @Column(name = "transactiondate", nullable = false)
    protected LocalDate transactionDate;

    @Column(name = "transactiontime", nullable = false)
    protected LocalTime transactionTime;
}
