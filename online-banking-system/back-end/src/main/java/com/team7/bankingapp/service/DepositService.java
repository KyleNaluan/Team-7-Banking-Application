package com.team7.bankingapp.service;

import com.team7.bankingapp.model.Account;
import com.team7.bankingapp.model.Deposit;
import com.team7.bankingapp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class DepositService {

    @Autowired
    private AccountRepository accountRepo;

    @Autowired
    private DepositRepository depositRepo;

    public Deposit makeDeposit(long accountId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than zero.");
        }

        Account account = accountRepo.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found."));

        account.setAccountBalance(account.getAccountBalance().add(amount));
        accountRepo.save(account);

        Deposit deposit = new Deposit();
        deposit.setAmount(amount);
        deposit.setTransactionDate(LocalDate.now());
        deposit.setTransactionTime(LocalTime.now());
        deposit.setAccount(account);

        return depositRepo.save(deposit);
    }
}
