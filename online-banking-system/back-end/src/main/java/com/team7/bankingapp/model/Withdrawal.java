package com.team7.bankingapp.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Withdrawal")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Withdrawal extends Transaction {
    @ManyToOne
    @JoinColumn(name = "accountid", nullable = false)
    private Account account;
}
