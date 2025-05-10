package com.team7.bankingapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team7.bankingapp.model.Account;
import com.team7.bankingapp.model.Customer;
import com.team7.bankingapp.model.Deposit;
import com.team7.bankingapp.service.DepositService;

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

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DepositController.class)
@AutoConfigureMockMvc(addFilters = false)
public class DepositControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DepositService depositService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testMakeDeposit_success() throws Exception {
        Customer customer = new Customer();
        customer.setCustomerID(1L);

        Account account = new Account();
        account.setAccountID(1L);
        account.setAccountType("Checking");
        account.setAccountBalance(new BigDecimal("100.00"));
        account.setCustomer(customer);

        Deposit deposit = new Deposit();
        deposit.setAmount(new BigDecimal("50.00"));
        deposit.setTransactionDate(LocalDate.now());
        deposit.setTransactionTime(LocalTime.now());
        deposit.setAccount(account);

        when(depositService.makeDeposit(eq(1L), eq(new BigDecimal("50.00")))).thenReturn(deposit);

        mockMvc.perform(post("/api/deposits")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "accountId", 1,
                        "amount", 50.00))))
                .andExpect(status().isOk());
    }

    @Test
    void testMakeDeposit_negativeAmount_returns400() throws Exception {
        when(depositService.makeDeposit(eq(1L), any(BigDecimal.class)))
                .thenAnswer(invocation -> {
                    BigDecimal amt = invocation.getArgument(1);
                    if (amt.compareTo(BigDecimal.ZERO) <= 0) {
                        throw new IllegalArgumentException("Deposit amount must be greater than zero.");
                    }
                    return null;
                });

        mockMvc.perform(post("/api/deposits")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "accountId", 1,
                        "amount", -10.00))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Deposit amount must be greater than zero."));
    }

    @Test
    void testMakeDeposit_accountNotFound_returns404() throws Exception {
        when(depositService.makeDeposit(eq(99L), any(BigDecimal.class)))
                .thenThrow(new RuntimeException("Account not found."));

        mockMvc.perform(post("/api/deposits")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "accountId", 99,
                        "amount", 25.00))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Account not found."));
    }
}
