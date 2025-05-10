package com.team7.bankingapp.controller;

import com.team7.bankingapp.model.Customer;
import com.team7.bankingapp.repository.PaymentRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/paymentstats")
public class PaymentStatsController {

    @Autowired
    private PaymentRepository paymentRepository;

    @GetMapping("/totals")
    public ResponseEntity<?> getSpendingTotals(@RequestParam String period, HttpSession session) {
        Customer customer = (Customer) session.getAttribute("customer");
        if (customer == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not logged in"));
        }

        LocalDate startDate;
        LocalDate endDate = LocalDate.now();

        switch (period) {
            case "Past 7 Days":
                startDate = endDate.minusDays(7);
                break;
            case "Past 30 Days":
                startDate = endDate.minusDays(30);
                break;
            case "Past 6 Months":
                startDate = endDate.minusMonths(6);
                break;
            case "Current Month":
                startDate = LocalDate.now().withDayOfMonth(1);
                endDate = startDate.plusMonths(1).minusDays(1);
                break;
            default:
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid time period specified"));
        }

        List<Object[]> categoryResults = paymentRepository.findCategorySpendingByCustomerAndDateRange(
                customer.getCustomerID(), startDate, endDate);

        List<Map<String, Object>> categorySpending = new ArrayList<>();
        for (Object[] row : categoryResults) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("category", row[0]);
            entry.put("amount", row[1]);
            categorySpending.add(entry);
        }

        BigDecimal totalSpent = paymentRepository.findTotalSpendingByCustomerAndDateRange(
                customer.getCustomerID(), startDate, endDate);

        return ResponseEntity.ok(Map.of(
                "categorySpending", categorySpending,
                "totalSpent", totalSpent != null ? totalSpent : BigDecimal.ZERO));
    }

    @GetMapping("/current-month")
    public ResponseEntity<?> getCurrentMonthSpending(HttpSession session) {
        Customer customer = (Customer) session.getAttribute("customer");
        if (customer == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not logged in"));
        }

        LocalDate startDate = LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        BigDecimal currentMonthSpent = paymentRepository.findTotalSpendingByCustomerAndDateRange(
                customer.getCustomerID(), startDate, endDate);

        return ResponseEntity.ok(Map.of(
                "currentMonthSpent", currentMonthSpent != null ? currentMonthSpent : BigDecimal.ZERO));
    }

    @GetMapping("/current-month-categories")
    public ResponseEntity<?> getCurrentMonthCategorySpending(HttpSession session) {
        Customer customer = (Customer) session.getAttribute("customer");
        if (customer == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not logged in"));
        }

        LocalDate startDate = LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        List<Object[]> categorySums = paymentRepository.findCategorySpendingByCustomerAndDateRange(
                customer.getCustomerID(), startDate, endDate);

        List<Map<String, Object>> categorySpending = new ArrayList<>();
        for (Object[] row : categorySums) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("category", row[0]);
            entry.put("amount", row[1]);
            categorySpending.add(entry);
        }

        return ResponseEntity.ok(categorySpending);
    }
}
