package com.team7.bankingapp.service;

import com.team7.bankingapp.model.Account;
import com.team7.bankingapp.model.Customer;
import com.team7.bankingapp.model.Transfer;
import com.team7.bankingapp.repository.AccountRepository;
import com.team7.bankingapp.repository.TransferRepository;

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
class TransferServiceTest {

    @Mock
    private AccountRepository accountRepo;

    @Mock
    private TransferRepository transferRepo;

    @InjectMocks
    private TransferService transferService;

    private Account sender;
    private Account receiver;

    @BeforeEach
    void setup() {
        Customer customer = new Customer();
        customer.setCustomerID(1L);

        sender = new Account();
        sender.setAccountID(1L);
        sender.setAccountBalance(new BigDecimal("500.00"));
        sender.setAccountType("Checking");
        sender.setCustomer(customer);

        receiver = new Account();
        receiver.setAccountID(2L);
        receiver.setAccountBalance(new BigDecimal("300.00"));
        receiver.setAccountType("Savings");
        receiver.setCustomer(customer);
    }

    @Test
    void testExecuteTransfer_successful() {
        BigDecimal amount = new BigDecimal("100.00");

        when(accountRepo.findById(1L)).thenReturn(Optional.of(sender));
        when(accountRepo.findById(2L)).thenReturn(Optional.of(receiver));
        when(transferRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        Transfer transfer = transferService.executeTransfer(1L, 2L, amount, null);

        assertEquals(new BigDecimal("400.00"), sender.getAccountBalance());
        assertEquals(new BigDecimal("400.00"), receiver.getAccountBalance());
        assertEquals(amount, transfer.getAmount());
        assertNull(transfer.getComment());
    }

    @Test
    void testExecuteTransfer_negativeAmount_throwsException() {
        when(accountRepo.findById(1L)).thenReturn(Optional.of(sender));
        when(accountRepo.findById(2L)).thenReturn(Optional.of(receiver));

        assertThrows(IllegalArgumentException.class,
                () -> transferService.executeTransfer(1L, 2L, new BigDecimal("-50.00"), null));
    }

    @Test
    void testExecuteTransfer_insufficientBalance_throwsException() {
        when(accountRepo.findById(1L)).thenReturn(Optional.of(sender));
        when(accountRepo.findById(2L)).thenReturn(Optional.of(receiver));

        assertThrows(IllegalArgumentException.class,
                () -> transferService.executeTransfer(1L, 2L, new BigDecimal("1000.00"), null));
    }

    @Test
    void testExecuteTransfer_sameAccount_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> transferService.executeTransfer(1L, 1L, new BigDecimal("50.00"), null));
    }

    @Test
    void testExecuteTransfer_senderNotFound_throwsException() {
        when(accountRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> transferService.executeTransfer(1L, 2L, new BigDecimal("50.00"), null));
    }

    @Test
    void testExecuteTransfer_receiverNotFound_throwsException() {
        when(accountRepo.findById(1L)).thenReturn(Optional.of(sender));
        when(accountRepo.findById(2L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> transferService.executeTransfer(1L, 2L, new BigDecimal("50.00"), null));
    }
}
