package com.team7.bankingapp.service;

import com.team7.bankingapp.model.Account;
import com.team7.bankingapp.model.Category;
import com.team7.bankingapp.model.Customer;
import com.team7.bankingapp.model.Payment;
import com.team7.bankingapp.repository.AccountRepository;
import com.team7.bankingapp.repository.CategoryRepository;
import com.team7.bankingapp.repository.PaymentRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepo;

    @Mock
    private AccountRepository accountRepo;

    @Mock
    private CategoryRepository categoryRepo;

    @InjectMocks
    private PaymentService paymentService;

    private Account account;
    private Category category;

    @BeforeEach
    void setup() {
        Customer customer = new Customer();
        customer.setCustomerID(1L);

        account = new Account();
        account.setAccountID(1L);
        account.setAccountBalance(new BigDecimal("200.00"));
        account.setAccountType("Checking");
        account.setCustomer(customer);

        category = new Category();
        category.setCategoryID(10);
        category.setCategoryName("Utilities");
    }

    @Test
    void testCreatePayment_success() {
        BigDecimal amount = new BigDecimal("50.00");

        when(accountRepo.findById(1L)).thenReturn(Optional.of(account));
        when(categoryRepo.findById(10)).thenReturn(Optional.of(category));
        when(accountRepo.save(any())).thenReturn(account);
        when(paymentRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Payment payment = paymentService.createPayment(1L, 10, amount, "Monthly utility bill");

        assertEquals(new BigDecimal("150.00"), account.getAccountBalance());
        assertEquals(amount, payment.getAmount());
        assertEquals("Monthly utility bill", payment.getComment());
        assertEquals(account, payment.getAccount());
        assertEquals(category, payment.getCategory());
    }

    @Test
    void testCreatePayment_negativeAmount_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> paymentService.createPayment(1L, 10, new BigDecimal("-20.00"), "Bad Payment"));
    }

    @Test
    void testCreatePayment_accountNotFound_throwsException() {
        when(accountRepo.findById(2L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> paymentService.createPayment(2L, 10, new BigDecimal("25.00"), null));
    }

    @Test
    void testCreatePayment_insufficientBalance_throwsException() {
        when(accountRepo.findById(1L)).thenReturn(Optional.of(account));

        assertThrows(IllegalArgumentException.class,
                () -> paymentService.createPayment(1L, 10, new BigDecimal("300.00"), "Too expensive"));
    }

    @Test
    void testCreatePayment_categoryNotFound_throwsException() {
        when(accountRepo.findById(1L)).thenReturn(Optional.of(account));
        when(categoryRepo.findById(99)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> paymentService.createPayment(1L, 99, new BigDecimal("50.00"), "Unknown category"));
    }

    @Test
    void testDeletePayment_success() {
        Payment payment = new Payment();
        payment.setTransactionID(101);
        payment.setAmount(new BigDecimal("75.00"));
        payment.setAccount(account);

        when(paymentRepo.findById(101)).thenReturn(Optional.of(payment));

        paymentService.deletePayment(101);

        assertEquals(new BigDecimal("275.00"), account.getAccountBalance());
        verify(accountRepo).save(account);
        verify(paymentRepo).delete(payment);
    }

    @Test
    void testDeletePayment_notFound_throwsException() {
        when(paymentRepo.findById(999)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> paymentService.deletePayment(999));
    }

}
