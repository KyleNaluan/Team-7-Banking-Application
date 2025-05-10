package com.team7.bankingapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team7.bankingapp.model.Customer;
import com.team7.bankingapp.model.TotalBudget;
import com.team7.bankingapp.service.TotalBudgetService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TotalBudgetController.class)
@AutoConfigureMockMvc(addFilters = false)
class TotalBudgetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TotalBudgetService totalBudgetService;

    @Autowired
    private ObjectMapper objectMapper;

    private Customer customer;
    private MockHttpSession session;

    @BeforeEach
    void setup() {
        customer = new Customer();
        customer.setCustomerID(1L);

        session = new MockHttpSession();
        session.setAttribute("customer", customer);
    }

    @Test
    void testCreateOrUpdateTotalBudget_success() throws Exception {
        TotalBudget budget = new TotalBudget(customer, 5, 2025, new BigDecimal("500.00"));

        when(totalBudgetService.setOrUpdateTotalBudget(eq(customer), eq(new BigDecimal("500.00"))))
                .thenReturn(budget);

        mockMvc.perform(post("/api/totalbudgets")
                .param("amount", "500.00")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monthlyTotalLimit").value(500.00));
    }

    @Test
    void testCreateOrUpdateTotalBudget_invalidAmount_returns400() throws Exception {
        when(totalBudgetService.setOrUpdateTotalBudget(eq(customer), eq(new BigDecimal("-100.00"))))
                .thenThrow(new IllegalArgumentException("Amount must be greater than zero."));

        mockMvc.perform(post("/api/totalbudgets")
                .param("amount", "-100.00")
                .session(session))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Amount must be greater than zero."));
    }

    @Test
    void testCreateOrUpdateTotalBudget_unauthorized_returns401() throws Exception {
        mockMvc.perform(post("/api/totalbudgets")
                .param("amount", "300.00"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("User not logged in"));
    }

    @Test
    void testGetCurrentMonthBudget_success() throws Exception {
        TotalBudget budget = new TotalBudget(customer, 5, 2025, new BigDecimal("300.00"));

        when(totalBudgetService.getCurrentMonthTotalBudget(1L)).thenReturn(Optional.of(budget));

        mockMvc.perform(get("/api/totalbudgets/current")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monthlyTotalLimit").value(300.00));
    }

    @Test
    void testGetCurrentMonthBudget_notFound_returns404() throws Exception {
        when(totalBudgetService.getCurrentMonthTotalBudget(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/totalbudgets/current")
                .session(session))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("No budget set for the current month"));
    }

    @Test
    void testGetCurrentMonthBudget_unauthorized_returns401() throws Exception {
        mockMvc.perform(get("/api/totalbudgets/current"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("User not logged in"));
    }

    @Test
    void testGetBudgets_returnsList() throws Exception {
        TotalBudget b1 = new TotalBudget(customer, 4, 2025, new BigDecimal("400.00"));
        TotalBudget b2 = new TotalBudget(customer, 5, 2025, new BigDecimal("600.00"));

        when(totalBudgetService.getTotalBudgetsForCustomer(1L)).thenReturn(List.of(b1, b2));

        mockMvc.perform(get("/api/totalbudgets")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
