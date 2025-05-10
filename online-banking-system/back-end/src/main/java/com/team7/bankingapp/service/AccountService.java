package com.team7.bankingapp.service;

import com.team7.bankingapp.model.Account;
import com.team7.bankingapp.repository.AccountRepository;
import com.team7.bankingapp.repository.DepositRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepo;
    @Autowired
    private DepositRepository depositRepo;

    private long generateUniqueAccountID() {
        Random random = new Random();
        long id;
        do {
            id = 1000000000L + (long) (random.nextDouble() * 9000000000L);
        } while (accountRepo.existsById(id));
        return id;
    }

    public Account createAccount(Account account) {
        account.setAccountID(generateUniqueAccountID());
        return accountRepo.save(account);
    }

    public List<Account> getAccountsByCustomer(long customerID) {
        return accountRepo.findByCustomerCustomerID(customerID);
    }

    public Account getAccountById(long id) {
        return accountRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }

}
