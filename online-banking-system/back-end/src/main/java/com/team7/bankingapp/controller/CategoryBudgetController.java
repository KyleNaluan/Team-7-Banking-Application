package com.team7.bankingapp.controller;

import com.team7.bankingapp.model.Customer;
import com.team7.bankingapp.model.Category;
import com.team7.bankingapp.model.CategoryBudget;
import com.team7.bankingapp.repository.CategoryRepository;
import com.team7.bankingapp.service.CategoryBudgetService;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/categorybudgets")
public class CategoryBudgetController {

    @Autowired
    private CategoryBudgetService categoryBudgetService;

    @Autowired
    private CategoryRepository categoryRepo;

    @PostMapping
    public ResponseEntity<?> createOrUpdateCategoryBudget(
            @RequestParam int categoryId,
            @RequestParam BigDecimal amount,
            HttpSession session) {

        Customer customer = (Customer) session.getAttribute("customer");
        if (customer == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not logged in"));
        }

        Category category = categoryRepo.findById(categoryId).orElse(null);
        if (category == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Category not found"));
        }

        try {
            CategoryBudget saved = categoryBudgetService.setOrUpdateCategoryBudget(customer, category, amount);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getBudgets(HttpSession session) {
        Customer customer = (Customer) session.getAttribute("customer");
        if (customer == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not logged in"));
        }

        LocalDate now = LocalDate.now();
        List<CategoryBudget> budgets = categoryBudgetService.getCategoryBudgetsForCustomerMonth(
                customer.getCustomerID(), now.getMonthValue(), now.getYear());

        return ResponseEntity.ok(budgets);
    }

}
