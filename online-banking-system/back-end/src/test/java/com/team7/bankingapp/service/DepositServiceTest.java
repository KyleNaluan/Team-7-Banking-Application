package com.team7.bankingapp.service;

import com.team7.bankingapp.model.Account;
import com.team7.bankingapp.model.Customer;
import com.team7.bankingapp.model.Deposit;
import com.team7.bankingapp.repository.AccountRepository;
import com.team7.bankingapp.repository.DepositRepository;
import org.junit.jupiter.api.BeforeEach;
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
class DepositServiceTest {

    @Mock
    private AccountRepository accountRepo;

    @Mock
    private DepositRepository depositRepo;

    @InjectMocks
    private DepositService depositService;

    private Account account;

    @BeforeEach
    void setup() {
        Customer customer = new Customer();
        customer.setCustomerID(1L);

        account = new Account();
        account.setAccountID(1L);
        account.setAccountBalance(new BigDecimal("100.00"));
        account.setAccountType("Checking");
        account.setCustomer(customer);
    }

    @Test
    void testMakeDeposit_successfulDeposit() {
        BigDecimal depositAmount = new BigDecimal("50.00");

        when(accountRepo.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepo.save(any(Account.class))).thenReturn(account);
        when(depositRepo.save(any(Deposit.class))).thenAnswer(i -> i.getArgument(0));

        Deposit result = depositService.makeDeposit(1L, depositAmount);

        assertEquals(new BigDecimal("150.00"), account.getAccountBalance());
        assertEquals(depositAmount, result.getAmount());
        assertEquals(account, result.getAccount());
        verify(accountRepo).save(account);
        verify(depositRepo).save(any(Deposit.class));
    }

    @Test
    void testMakeDeposit_negativeAmount_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> depositService.makeDeposit(1L, new BigDecimal("-10.00")));
    }

    @Test
    void testMakeDeposit_accountNotFound_throwsException() {
        when(accountRepo.findById(2L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> depositService.makeDeposit(2L, new BigDecimal("25.00")));
    }
}
