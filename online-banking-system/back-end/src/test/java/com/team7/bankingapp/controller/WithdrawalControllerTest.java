package com.team7.bankingapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team7.bankingapp.model.Account;
import com.team7.bankingapp.model.Customer;
import com.team7.bankingapp.model.Withdrawal;
import com.team7.bankingapp.service.WithdrawalService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WithdrawalController.class)
@AutoConfigureMockMvc(addFilters = false)
public class WithdrawalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WithdrawalService withdrawalService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testWithdrawal_success() throws Exception {
        Customer customer = new Customer();
        customer.setCustomerID(1L);

        Account account = new Account();
        account.setAccountID(1L);
        account.setAccountBalance(new BigDecimal("100.00"));
        account.setAccountType("Checking");
        account.setCustomer(customer);

        Withdrawal withdrawal = new Withdrawal();
        withdrawal.setAmount(new BigDecimal("30.00"));
        withdrawal.setTransactionDate(LocalDate.now());
        withdrawal.setTransactionTime(LocalTime.now());
        withdrawal.setAccount(account);

        when(withdrawalService.createWithdrawal(eq(1L), any(BigDecimal.class))).thenReturn(withdrawal);

        mockMvc.perform(post("/api/withdrawals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "accountId", 1,
                        "amount", 30.00))))
                .andExpect(status().isOk());
    }

    @Test
    void testWithdrawal_negativeAmount_returns400() throws Exception {
        when(withdrawalService.createWithdrawal(eq(1L), any(BigDecimal.class)))
                .thenThrow(new IllegalArgumentException("Withdrawal amount must be positive."));

        mockMvc.perform(post("/api/withdrawals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "accountId", 1,
                        "amount", -25.00))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Withdrawal amount must be positive."));
    }

    @Test
    void testWithdrawal_insufficientBalance_returns400() throws Exception {
        when(withdrawalService.createWithdrawal(eq(1L), any(BigDecimal.class)))
                .thenThrow(new IllegalArgumentException("Insufficient balance for withdrawal."));

        mockMvc.perform(post("/api/withdrawals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "accountId", 1,
                        "amount", 1000.00))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Insufficient balance for withdrawal."));
    }

    @Test
    void testWithdrawal_accountNotFound_returns404() throws Exception {
        when(withdrawalService.createWithdrawal(eq(99L), any(BigDecimal.class)))
                .thenThrow(new RuntimeException("Account not found."));

        mockMvc.perform(post("/api/withdrawals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "accountId", 99,
                        "amount", 50.00))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Account not found."));
    }
}
