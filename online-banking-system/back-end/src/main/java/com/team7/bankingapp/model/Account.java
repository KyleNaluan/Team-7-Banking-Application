package com.team7.bankingapp.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;

@Entity
@Table(name = "Account")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    @Id
    private long accountID;

    @Column(name = "accountbalance", nullable = false)
    private BigDecimal accountBalance = BigDecimal.ZERO;

    @Column(name = "accounttype", nullable = false)
    private String accountType;

    @ManyToOne
    @JoinColumn(name = "customerid", nullable = false)
    private Customer customer;

    @Override
    public String toString() {
        return "Account{" +
                "accountID=" + accountID +
                ", accountBalance=" + accountBalance +
                ", accountType='" + accountType + '\'' +
                ", customer=" + (customer != null ? customer.getCustomerID() : "null") +
                '}';
    }

}
