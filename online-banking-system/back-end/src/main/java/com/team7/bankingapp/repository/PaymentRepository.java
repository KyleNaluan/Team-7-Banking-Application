package com.team7.bankingapp.repository;

import com.team7.bankingapp.model.Payment;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    List<Payment> findByAccountAccountID(long accountId);

    @Query("SELECT p.category.categoryName, SUM(p.amount) " +
            "FROM Payment p " +
            "WHERE p.account.customer.customerID = :customerId " +
            "AND p.transactionDate BETWEEN :startDate AND :endDate " +
            "GROUP BY p.category.categoryName")
    List<Object[]> findCategorySpendingByCustomerAndDateRange(
            @Param("customerId") long customerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(p.amount) " +
            "FROM Payment p " +
            "WHERE p.account.customer.customerID = :customerId " +
            "AND p.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal findTotalSpendingByCustomerAndDateRange(
            @Param("customerId") long customerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
