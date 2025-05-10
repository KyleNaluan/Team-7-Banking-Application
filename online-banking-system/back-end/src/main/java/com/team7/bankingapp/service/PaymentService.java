package com.team7.bankingapp.service;

import com.team7.bankingapp.model.Account;
import com.team7.bankingapp.model.Category;
import com.team7.bankingapp.model.Payment;
import com.team7.bankingapp.repository.AccountRepository;
import com.team7.bankingapp.repository.CategoryRepository;
import com.team7.bankingapp.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepo;

    @Autowired
    private AccountRepository accountRepo;

    @Autowired
    private CategoryRepository categoryRepo;

    public Payment createPayment(long accountID, int categoryID, BigDecimal amount, String comment) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }

        Account account = accountRepo.findById(accountID)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (account.getAccountBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance for payment.");
        }

        Category category = categoryRepo.findById(categoryID)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        account.setAccountBalance(account.getAccountBalance().subtract(amount));
        accountRepo.save(account);

        Payment payment = new Payment();
        payment.setAmount(amount);
        payment.setTransactionDate(LocalDate.now());
        payment.setTransactionTime(LocalTime.now());
        payment.setComment(comment);
        payment.setAccount(account);
        payment.setCategory(category);

        return paymentRepo.save(payment);
    }

    public void deletePayment(int paymentId) {
        Optional<Payment> optionalPayment = paymentRepo.findById(paymentId);
        if (optionalPayment.isEmpty()) {
            throw new RuntimeException("Payment not found");
        }

        Payment payment = optionalPayment.get();
        Account account = payment.getAccount();

        account.setAccountBalance(account.getAccountBalance().add(payment.getAmount()));
        accountRepo.save(account);
        paymentRepo.delete(payment);
    }
}
