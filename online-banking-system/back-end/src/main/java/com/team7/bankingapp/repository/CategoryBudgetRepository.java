package com.team7.bankingapp.repository;

import com.team7.bankingapp.model.CategoryBudget;
import com.team7.bankingapp.model.CategoryBudgetId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CategoryBudgetRepository extends JpaRepository<CategoryBudget, CategoryBudgetId> {

    Optional<CategoryBudget> findByCustomerCustomerIDAndCategoryCategoryIDAndMonthAndYear(
            long customerID, int categoryID, int month, int year);

    List<CategoryBudget> findByCustomerCustomerIDAndMonthAndYear(long customerID, int month, int year);
}
