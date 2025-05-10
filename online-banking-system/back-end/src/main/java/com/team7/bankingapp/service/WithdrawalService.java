package com.team7.bankingapp.service;

import com.team7.bankingapp.model.Account;
import com.team7.bankingapp.model.Withdrawal;
import com.team7.bankingapp.repository.AccountRepository;
import com.team7.bankingapp.repository.WithdrawalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class WithdrawalService {

    @Autowired
    private AccountRepository accountRepo;

    @Autowired
    private WithdrawalRepository withdrawalRepo;

    public Withdrawal createWithdrawal(long accountId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive.");
        }

        Account account = accountRepo.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found."));

        if (account.getAccountBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance for withdrawal.");
        }

        account.setAccountBalance(account.getAccountBalance().subtract(amount));
        accountRepo.save(account);

        Withdrawal withdrawal = new Withdrawal();
        withdrawal.setAmount(amount);
        withdrawal.setTransactionDate(LocalDate.now());
        withdrawal.setTransactionTime(LocalTime.now());
        withdrawal.setAccount(account);

        return withdrawalRepo.save(withdrawal);
    }
}
