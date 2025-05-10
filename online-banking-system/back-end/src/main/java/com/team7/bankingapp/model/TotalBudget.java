package com.team7.bankingapp.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "totalbudget")
@IdClass(TotalBudgetId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TotalBudget {

    @Id
    @ManyToOne
    @JoinColumn(name = "customerid", nullable = false)
    private Customer customer;

    @Id
    @Column(nullable = false)
    private int month;

    @Id
    @Column(nullable = false)
    private int year;

    @Column(name = "monthlytotallimit", nullable = false)
    private BigDecimal monthlyTotalLimit;
}
