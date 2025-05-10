package com.team7.bankingapp.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Deposit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Deposit extends Transaction {
    @ManyToOne
    @JoinColumn(name = "accountid", nullable = false)
    private Account account;
}
