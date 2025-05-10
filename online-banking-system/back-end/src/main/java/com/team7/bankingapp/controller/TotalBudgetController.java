package com.team7.bankingapp.controller;

import com.team7.bankingapp.model.Customer;
import com.team7.bankingapp.model.TotalBudget;
import com.team7.bankingapp.service.TotalBudgetService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/totalbudgets")
public class TotalBudgetController {

    @Autowired
    private TotalBudgetService totalBudgetService;

    @PostMapping
    public ResponseEntity<?> createOrUpdateTotalBudget(
            @RequestParam BigDecimal amount,
            HttpSession session) {

        Customer customer = (Customer) session.getAttribute("customer");
        if (customer == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not logged in"));
        }

        try {
            TotalBudget saved = totalBudgetService.setOrUpdateTotalBudget(customer, amount);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public List<TotalBudget> getBudgets(HttpSession session) {
        Customer customer = (Customer) session.getAttribute("customer");
        return totalBudgetService.getTotalBudgetsForCustomer(customer.getCustomerID());
    }

    @GetMapping("/current")
    public ResponseEntity<?> getCurrentMonthBudget(HttpSession session) {
        Customer customer = (Customer) session.getAttribute("customer");

        if (customer == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not logged in"));
        }

        Optional<TotalBudget> budgetOpt = totalBudgetService.getCurrentMonthTotalBudget(customer.getCustomerID());

        if (budgetOpt.isPresent()) {
            return ResponseEntity.ok(budgetOpt.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No budget set for the current month"));
        }
    }

}
