package com.team7.bankingapp.repository;

import com.team7.bankingapp.model.TotalBudget;
import com.team7.bankingapp.model.TotalBudgetId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface TotalBudgetRepository extends JpaRepository<TotalBudget, TotalBudgetId> {

    Optional<TotalBudget> findByCustomerCustomerIDAndMonthAndYear(long customerID, int month, int year);

    List<TotalBudget> findByCustomerCustomerID(long customerID);
}
