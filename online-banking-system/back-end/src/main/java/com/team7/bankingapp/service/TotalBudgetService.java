package com.team7.bankingapp.service;

import com.team7.bankingapp.model.TotalBudget;
import com.team7.bankingapp.model.CategoryBudget;
import com.team7.bankingapp.model.Customer;
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
public class TotalBudgetService {

    @Autowired
    private TotalBudgetRepository totalBudgetRepo;

    @Autowired
    private CategoryBudgetRepository categoryBudgetRepo;

    @Autowired
    private CustomerRepository customerRepo;

    public TotalBudget setOrUpdateTotalBudget(Customer customer, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Total budget amount must be greater than zero.");
        }

        LocalDate now = LocalDate.now();
        int month = now.getMonthValue();
        int year = now.getYear();
        long customerId = customer.getCustomerID();

        Customer managedCustomer = customerRepo.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        List<CategoryBudget> existingCategoryBudgets = categoryBudgetRepo
                .findByCustomerCustomerIDAndMonthAndYear(customerId, month, year);

        BigDecimal totalCategoryBudgets = existingCategoryBudgets.stream()
                .map(CategoryBudget::getMonthlyLimit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (amount.compareTo(totalCategoryBudgets) < 0) {
            throw new IllegalArgumentException(
                    "Total budget cannot be less than the combined total of existing category budgets ($"
                            + totalCategoryBudgets + "). Please reduce category budgets first.");
        }

        Optional<TotalBudget> existingBudget = totalBudgetRepo.findByCustomerCustomerIDAndMonthAndYear(customerId,
                month, year);

        TotalBudget budget = existingBudget.orElse(new TotalBudget());
        budget.setCustomer(managedCustomer);
        budget.setMonth(month);
        budget.setYear(year);
        budget.setMonthlyTotalLimit(amount);

        return totalBudgetRepo.save(budget);
    }

    public List<TotalBudget> getTotalBudgetsForCustomer(long customerId) {
        return totalBudgetRepo.findByCustomerCustomerID(customerId);
    }

    public Optional<TotalBudget> getCurrentMonthTotalBudget(long customerId) {
        LocalDate now = LocalDate.now();
        return totalBudgetRepo.findByCustomerCustomerIDAndMonthAndYear(customerId, now.getMonthValue(), now.getYear());
    }
}
