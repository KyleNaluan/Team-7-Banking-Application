package com.team7.bankingapp.controller;

import com.team7.bankingapp.model.Account;
import com.team7.bankingapp.model.Customer;
import com.team7.bankingapp.service.AccountService;
import com.team7.bankingapp.service.TransferService;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;
    @Autowired
    private TransferService transferService;

    @PostMapping("/create")
    public ResponseEntity<Account> createAccount(@RequestBody Account account, HttpSession session) {
        Object userObj = session.getAttribute("customer");
        if (userObj == null) {
            return ResponseEntity.status(401).build();
        }

        Customer customer = (Customer) userObj;
        account.setCustomer(customer);
        System.out.println("Creating account with type: " + account.getAccountType());
        System.out.println("Saving account: " + account);

        return ResponseEntity.ok(accountService.createAccount(account));
    }

    @GetMapping("/customer/{customerID}")
    public ResponseEntity<List<Account>> getAccountsByCustomer(@PathVariable long customerID) {
        return ResponseEntity.ok(accountService.getAccountsByCustomer(customerID));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> getAccountById(@PathVariable long id) {
        return ResponseEntity.ok(
                accountService.getAccountById(id));
    }

}
