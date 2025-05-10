package com.team7.bankingapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team7.bankingapp.model.Account;
import com.team7.bankingapp.model.Customer;
import com.team7.bankingapp.model.Transfer;
import com.team7.bankingapp.service.TransferService;

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

@WebMvcTest(TransferController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransferService transferService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testTransfer_success() throws Exception {
        Customer customer = new Customer();
        customer.setCustomerID(1L);

        Account sender = new Account();
        sender.setAccountID(1L);
        sender.setAccountType("Checking");
        sender.setAccountBalance(new BigDecimal("500.00"));
        sender.setCustomer(customer);

        Account receiver = new Account();
        receiver.setAccountID(2L);
        receiver.setAccountType("Savings");
        receiver.setAccountBalance(new BigDecimal("300.00"));
        receiver.setCustomer(customer);

        Transfer transfer = new Transfer();
        transfer.setAmount(new BigDecimal("100.00"));
        transfer.setTransactionDate(LocalDate.now());
        transfer.setTransactionTime(LocalTime.now());
        transfer.setComment("Rent");
        transfer.setSender(sender);
        transfer.setReceiver(receiver);

        when(transferService.executeTransfer(eq(1L), eq(2L), any(BigDecimal.class), eq("Rent"))).thenReturn(transfer);

        mockMvc.perform(post("/api/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "sourceAccount", 1,
                        "receivingAccount", 2,
                        "amount", 100.00,
                        "comment", "Rent"))))
                .andExpect(status().isOk());
    }

    @Test
    void testTransfer_invalidAmount_returns400() throws Exception {
        when(transferService.executeTransfer(eq(1L), eq(2L), any(BigDecimal.class), any()))
                .thenThrow(new IllegalArgumentException("Transfer amount must be positive."));

        mockMvc.perform(post("/api/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "sourceAccount", 1,
                        "receivingAccount", 2,
                        "amount", -50.00))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Transfer amount must be positive."));
    }

    @Test
    void testTransfer_sameAccount_returns400() throws Exception {
        when(transferService.executeTransfer(eq(1L), eq(1L), any(BigDecimal.class), any()))
                .thenThrow(new IllegalArgumentException("Source and receiving account cannot be the same."));

        mockMvc.perform(post("/api/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "sourceAccount", 1,
                        "receivingAccount", 1,
                        "amount", 100.00))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Source and receiving account cannot be the same."));
    }

    @Test
    void testTransfer_accountNotFound_returns404() throws Exception {
        when(transferService.executeTransfer(eq(1L), eq(2L), any(BigDecimal.class), any()))
                .thenThrow(new RuntimeException("Source account not found"));

        mockMvc.perform(post("/api/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "sourceAccount", 1,
                        "receivingAccount", 2,
                        "amount", 100.00))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Source account not found"));
    }
}
