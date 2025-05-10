package com.team7.bankingapp.controller;

import com.team7.bankingapp.model.CategoryBudget;
import com.team7.bankingapp.model.Customer;
import com.team7.bankingapp.model.TotalBudget;
import com.team7.bankingapp.service.CategoryBudgetService;
import com.team7.bankingapp.service.TotalBudgetService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/ai")
public class AiBudgetAnalysisController {

    @Autowired
    private CategoryBudgetService categoryBudgetService;

    @Autowired
    private TotalBudgetService totalBudgetService;

    @Autowired
    private PaymentStatsController paymentStatsController;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${groq.api.key}")
    private String groqApiKey;

    @Value("${groq.api.url}")
    private String groqApiUrl;

    @Value("${groq.model}")
    private String groqModel;

    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeBudget(HttpSession session) {
        Customer customer = (Customer) session.getAttribute("customer");
        if (customer == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in.");
        }

        long customerId = customer.getCustomerID();
        int month = LocalDate.now().getMonthValue();
        int year = LocalDate.now().getYear();

        Optional<TotalBudget> totalBudgetOpt = totalBudgetService.getCurrentMonthTotalBudget(customerId);
        BigDecimal totalLimit = totalBudgetOpt.map(TotalBudget::getMonthlyTotalLimit).orElse(BigDecimal.ZERO);

        List<CategoryBudget> categoryBudgets = categoryBudgetService.getCategoryBudgetsForCustomerMonth(customerId,
                month, year);

        List<Map<String, Object>> categorySpendingList = (List<Map<String, Object>>) paymentStatsController
                .getCurrentMonthCategorySpending(session).getBody();

        Map<String, BigDecimal> categorySpendingMap = new HashMap<>();
        for (Map<String, Object> entry : categorySpendingList) {
            String category = (String) entry.get("category");
            BigDecimal amount = new BigDecimal(entry.get("amount").toString());
            categorySpendingMap.put(category, amount);
        }

        BigDecimal totalSpent = categorySpendingMap.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        StringBuilder prompt = new StringBuilder(
                "Provide a financial analysis for the user's current month budget and spending.\n\n");

        if (totalLimit.compareTo(BigDecimal.ZERO) > 0) {
            prompt.append(String.format("Total Budget: $%.2f\n", totalLimit));
            prompt.append(String.format("Total Spent: $%.2f\n\n", totalSpent));
        } else {
            prompt.append("No total budget has been set for this month.\n\n");
        }

        if (!categoryBudgets.isEmpty()) {
            prompt.append("Category Budgets and Spending:\n");

            for (CategoryBudget cb : categoryBudgets) {
                String name = cb.getCategory().getCategoryName();
                BigDecimal limit = cb.getMonthlyLimit();
                BigDecimal spent = categorySpendingMap.getOrDefault(name, BigDecimal.ZERO);
                prompt.append(String.format("- %s: Budget = $%.2f, Spent = $%.2f\n", name, limit, spent));
            }
        } else {
            prompt.append("No category budgets have been set.\n");
        }

        if (categorySpendingMap.isEmpty()) {
            prompt.append("\nNo spending has been recorded yet this month.\n");
        }

        prompt.append("\nProvide concise insights. Flag any overspending, or confirm if the user is within budget.");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(groqApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> message = Map.of("role", "user", "content", prompt.toString());
        Map<String, Object> payload = Map.of(
                "model", groqModel,
                "messages", List.of(message));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(groqApiUrl, entity, Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            Map<String, Object> messageObj = (Map<String, Object>) choices.get(0).get("message");
            String content = (String) messageObj.get("content");

            return ResponseEntity.ok(Map.of("analysis", content));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to get response from Groq.");
        }
    }
}
