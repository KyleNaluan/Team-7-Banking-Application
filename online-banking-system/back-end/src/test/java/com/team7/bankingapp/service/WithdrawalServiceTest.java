package com.team7.bankingapp.service;

import com.team7.bankingapp.model.Account;
import com.team7.bankingapp.model.Customer;
import com.team7.bankingapp.model.Withdrawal;
import com.team7.bankingapp.repository.AccountRepository;
import com.team7.bankingapp.repository.WithdrawalRepository;

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
class WithdrawalServiceTest {

    @Mock
    private AccountRepository accountRepo;

    @Mock
    private WithdrawalRepository withdrawalRepo;

    @InjectMocks
    private WithdrawalService withdrawalService;

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
    void testCreateWithdrawal_success() {
        BigDecimal withdrawalAmount = new BigDecimal("40.00");

        when(accountRepo.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepo.save(any())).thenReturn(account);
        when(withdrawalRepo.save(any(Withdrawal.class))).thenAnswer(inv -> inv.getArgument(0));

        Withdrawal withdrawal = withdrawalService.createWithdrawal(1L, withdrawalAmount);

        assertEquals(new BigDecimal("60.00"), account.getAccountBalance());
        assertEquals(withdrawalAmount, withdrawal.getAmount());
        assertEquals(account, withdrawal.getAccount());
    }

    @Test
    void testCreateWithdrawal_negativeAmount_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> withdrawalService.createWithdrawal(1L, new BigDecimal("-20.00")));
    }

    @Test
    void testCreateWithdrawal_insufficientBalance_throwsException() {
        when(accountRepo.findById(1L)).thenReturn(Optional.of(account));

        assertThrows(IllegalArgumentException.class,
                () -> withdrawalService.createWithdrawal(1L, new BigDecimal("200.00")));
    }

    @Test
    void testCreateWithdrawal_accountNotFound_throwsException() {
        when(accountRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> withdrawalService.createWithdrawal(99L, new BigDecimal("20.00")));
    }
}
