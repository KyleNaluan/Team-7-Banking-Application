package com.team7.bankingapp.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment extends Transaction {
    @Column(length = 600)
    private String comment;

    @ManyToOne
    @JoinColumn(name = "categoryid")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "accountid", nullable = false)
    private Account account;
}
