package com.team7.bankingapp.controller;

import com.team7.bankingapp.model.Account;
import com.team7.bankingapp.model.Customer;
import com.team7.bankingapp.service.AccountService;
import com.team7.bankingapp.service.TransferService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @MockBean
    private TransferService transferService;

    @Test
    void testGetAccountById_returnsJson() throws Exception {
        Customer customer = new Customer();
        customer.setCustomerID(1L);

        Account account = new Account();
        account.setAccountID(1L);
        account.setAccountBalance(new BigDecimal("150.00"));
        account.setAccountType("Savings");
        account.setCustomer(customer);

        when(accountService.getAccountById(1L)).thenReturn(account);

        mockMvc.perform(get("/api/accounts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountType").value("Savings"))
                .andExpect(jsonPath("$.accountBalance").value(150.00));
    }
}
