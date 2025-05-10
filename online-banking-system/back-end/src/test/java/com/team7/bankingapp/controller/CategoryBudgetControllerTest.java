package com.team7.bankingapp.controller;

import com.team7.bankingapp.model.Category;
import com.team7.bankingapp.model.CategoryBudget;
import com.team7.bankingapp.model.Customer;
import com.team7.bankingapp.repository.CategoryRepository;
import com.team7.bankingapp.service.CategoryBudgetService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryBudgetController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryBudgetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryBudgetService categoryBudgetService;

    @MockBean
    private CategoryRepository categoryRepo;

    private Customer customer;
    private Category category;
    private MockHttpSession session;

    @BeforeEach
    void setup() {
        customer = new Customer();
        customer.setCustomerID(1L);

        category = new Category();
        category.setCategoryID(10);
        category.setCategoryName("Food");

        session = new MockHttpSession();
        session.setAttribute("customer", customer);
    }

    @Test
    void testCreateOrUpdateCategoryBudget_success() throws Exception {
        CategoryBudget budget = new CategoryBudget(customer, category, 5, 2025, new BigDecimal("100.00"));

        when(categoryRepo.findById(10)).thenReturn(java.util.Optional.of(category));
        when(categoryBudgetService.setOrUpdateCategoryBudget(customer, category, new BigDecimal("100.00")))
                .thenReturn(budget);

        mockMvc.perform(post("/api/categorybudgets")
                .param("categoryId", "10")
                .param("amount", "100.00")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monthlyLimit").value(100.00));
    }

    @Test
    void testCreateOrUpdateCategoryBudget_negativeAmount_returns400() throws Exception {
        when(categoryRepo.findById(10)).thenReturn(java.util.Optional.of(category));
        when(categoryBudgetService.setOrUpdateCategoryBudget(eq(customer), eq(category), eq(new BigDecimal("-50.00"))))
                .thenThrow(new IllegalArgumentException("Category budget amount must be greater than zero."));

        mockMvc.perform(post("/api/categorybudgets")
                .param("categoryId", "10")
                .param("amount", "-50.00")
                .session(session))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Category budget amount must be greater than zero."));
    }

    @Test
    void testCreateOrUpdateCategoryBudget_notLoggedIn_returns401() throws Exception {
        mockMvc.perform(post("/api/categorybudgets")
                .param("categoryId", "10")
                .param("amount", "100.00"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("User not logged in"));
    }

    @Test
    void testCreateOrUpdateCategoryBudget_categoryNotFound_returns500() throws Exception {
        when(categoryRepo.findById(10)).thenReturn(java.util.Optional.empty());

        mockMvc.perform(post("/api/categorybudgets")
                .param("categoryId", "10")
                .param("amount", "100.00")
                .session(session))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Category not found"));

    }

    @Test
    void testGetBudgets_success() throws Exception {
        CategoryBudget b1 = new CategoryBudget(customer, category, 5, 2025, new BigDecimal("75.00"));

        int month = LocalDate.now().getMonthValue();
        int year = LocalDate.now().getYear();

        when(categoryBudgetService.getCategoryBudgetsForCustomerMonth(1L, month, year))
                .thenReturn(List.of(b1));

        mockMvc.perform(get("/api/categorybudgets").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testGetBudgets_notLoggedIn_returns401() throws Exception {
        mockMvc.perform(get("/api/categorybudgets"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("User not logged in"));
    }
}
