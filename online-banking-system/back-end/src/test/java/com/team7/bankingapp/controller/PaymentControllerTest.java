package com.team7.bankingapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team7.bankingapp.model.Account;
import com.team7.bankingapp.model.Category;
import com.team7.bankingapp.model.Customer;
import com.team7.bankingapp.model.Payment;
import com.team7.bankingapp.service.PaymentService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreatePayment_success() throws Exception {
        Customer customer = new Customer();
        customer.setCustomerID(1L);

        Account account = new Account();
        account.setAccountID(1L);
        account.setAccountBalance(new BigDecimal("200.00"));
        account.setAccountType("Checking");
        account.setCustomer(customer);

        Category category = new Category();
        category.setCategoryID(10);
        category.setCategoryName("Groceries");

        Payment payment = new Payment();
        payment.setAmount(new BigDecimal("75.00"));
        payment.setTransactionDate(LocalDate.now());
        payment.setTransactionTime(LocalTime.now());
        payment.setComment("Groceries for week");
        payment.setAccount(account);
        payment.setCategory(category);

        when(paymentService.createPayment(eq(1L), eq(10), any(BigDecimal.class), eq("Groceries for week")))
                .thenReturn(payment);

        mockMvc.perform(post("/api/payments")
                .param("sourceAccount", "1")
                .param("transactionCategory", "10")
                .param("amount", "75.00")
                .param("comment", "Groceries for week"))
                .andExpect(status().isOk());
    }

    @Test
    void testCreatePayment_negativeAmount_returns400() throws Exception {
        when(paymentService.createPayment(eq(1L), eq(10), any(BigDecimal.class), any()))
                .thenThrow(new IllegalArgumentException("Amount must be greater than zero."));

        mockMvc.perform(post("/api/payments")
                .param("sourceAccount", "1")
                .param("transactionCategory", "10")
                .param("amount", "-50.00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Amount must be greater than zero."));
    }

    @Test
    void testCreatePayment_accountNotFound_returns404() throws Exception {
        when(paymentService.createPayment(eq(2L), eq(10), any(BigDecimal.class), any()))
                .thenThrow(new RuntimeException("Account not found"));

        mockMvc.perform(post("/api/payments")
                .param("sourceAccount", "2")
                .param("transactionCategory", "10")
                .param("amount", "25.00"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Account not found"));
    }

    @Test
    void testDeletePayment_success() throws Exception {
        doNothing().when(paymentService).deletePayment(101);

        mockMvc.perform(delete("/api/payments/101"))
                .andExpect(status().isOk());
    }

    @Test
    void testDeletePayment_notFound_returns404() throws Exception {
        doThrow(new RuntimeException("Payment not found"))
                .when(paymentService).deletePayment(999);

        mockMvc.perform(delete("/api/payments/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Payment not found"));
    }
}
