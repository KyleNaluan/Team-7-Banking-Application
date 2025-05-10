package com.team7.bankingapp.service;

import com.team7.bankingapp.model.*;
import com.team7.bankingapp.repository.CategoryBudgetRepository;
import com.team7.bankingapp.repository.CustomerRepository;
import com.team7.bankingapp.repository.TotalBudgetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

@Service
public class CategoryBudgetService {

        @Autowired
        private CategoryBudgetRepository categoryBudgetRepo;

        @Autowired
        private TotalBudgetRepository totalBudgetRepo;

        @Autowired
        private CustomerRepository customerRepo;

        public CategoryBudget setOrUpdateCategoryBudget(Customer customer, Category category, BigDecimal amount) {
                if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                        throw new IllegalArgumentException("Category budget amount must be greater than zero.");
                }

                LocalDate now = LocalDate.now();
                int month = now.getMonthValue();
                int year = now.getYear();
                long customerId = customer.getCustomerID();

                Customer managedCustomer = customerRepo.findById(customerId)
                                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

                Optional<TotalBudget> totalBudgetOpt = totalBudgetRepo.findByCustomerCustomerIDAndMonthAndYear(
                                customerId,
                                month, year);
                if (totalBudgetOpt.isEmpty()) {
                        throw new IllegalArgumentException(
                                        "Total budget for this month does not exist. Please set it before assigning category budgets.");
                }
                TotalBudget totalBudget = totalBudgetOpt.get();

                Optional<CategoryBudget> existingBudgetOpt = categoryBudgetRepo
                                .findByCustomerCustomerIDAndCategoryCategoryIDAndMonthAndYear(
                                                customerId, category.getCategoryID(), month, year);

                BigDecimal existingAmount = existingBudgetOpt.map(CategoryBudget::getMonthlyLimit)
                                .orElse(BigDecimal.ZERO);

                List<CategoryBudget> currentBudgets = categoryBudgetRepo.findByCustomerCustomerIDAndMonthAndYear(
                                customerId,
                                month, year);
                BigDecimal currentTotal = currentBudgets.stream()
                                .map(CategoryBudget::getMonthlyLimit)
                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                                .subtract(existingAmount);

                BigDecimal remaining = totalBudget.getMonthlyTotalLimit().subtract(currentTotal);
                BigDecimal proposedTotal = currentTotal.add(amount);

                if (proposedTotal.compareTo(totalBudget.getMonthlyTotalLimit()) > 0) {
                        throw new IllegalArgumentException(
                                        String.format(
                                                        "Adding this category budget would exceed the total monthly budget. You have $%.2f remaining to allocate.",
                                                        remaining));
                }

                CategoryBudget budget = existingBudgetOpt.orElse(new CategoryBudget());
                budget.setCustomer(managedCustomer);
                budget.setCategory(category);
                budget.setMonth(month);
                budget.setYear(year);
                budget.setMonthlyLimit(amount);

                return categoryBudgetRepo.save(budget);
        }

        public List<CategoryBudget> getCategoryBudgetsForCustomerMonth(long customerId, int month, int year) {
                return categoryBudgetRepo.findByCustomerCustomerIDAndMonthAndYear(customerId, month, year);
        }
}
