package com.team7.bankingapp.service;

import com.team7.bankingapp.model.Account;
import com.team7.bankingapp.model.Customer;
import com.team7.bankingapp.repository.AccountRepository;
import com.team7.bankingapp.repository.DepositRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepo;

    @Mock
    private DepositRepository depositRepo;

    @InjectMocks
    private AccountService accountService;

    @Test
    void testGetAccountById_returnsCorrectAccount() {
        Customer customer = new Customer();
        customer.setCustomerID(1L);

        Account account = new Account();
        account.setAccountID(1L);
        account.setAccountBalance(new BigDecimal("500.00"));
        account.setAccountType("Checking");
        account.setCustomer(customer);

        when(accountRepo.findById(1L)).thenReturn(Optional.of(account));

        Account result = accountService.getAccountById(1L);

        assertNotNull(result);
        assertEquals("Checking", result.getAccountType());
        assertEquals(new BigDecimal("500.00"), result.getAccountBalance());
        assertEquals(1L, result.getCustomer().getCustomerID());
    }

    @Test
    void testGetAccountById_accountNotFound_throwsException() {
        when(accountRepo.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            accountService.getAccountById(999L);
        });

        assertEquals("Account not found", exception.getMessage());
    }
}
