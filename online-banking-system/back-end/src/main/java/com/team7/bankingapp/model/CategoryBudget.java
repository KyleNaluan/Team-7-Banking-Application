package com.team7.bankingapp.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "categorybudget")
@IdClass(CategoryBudgetId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryBudget {

    @Id
    @ManyToOne
    @JoinColumn(name = "customerid", nullable = false)
    private Customer customer;

    @Id
    @ManyToOne
    @JoinColumn(name = "categoryid", nullable = false)
    private Category category;

    @Id
    @Column(nullable = false)
    private int month;

    @Id
    @Column(nullable = false)
    private int year;

    @Column(name = "monthlylimit", nullable = false)
    private BigDecimal monthlyLimit;
}
