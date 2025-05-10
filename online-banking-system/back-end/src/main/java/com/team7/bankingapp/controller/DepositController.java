package com.team7.bankingapp.controller;

import com.team7.bankingapp.model.Deposit;
import com.team7.bankingapp.service.DepositService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/deposits")
public class DepositController {

    @Autowired
    private DepositService depositService;

    @PostMapping
    public ResponseEntity<?> makeDeposit(@RequestBody Map<String, Object> payload) {
        try {
            long accountId = Long.parseLong(payload.get("accountId").toString());
            BigDecimal amount = new BigDecimal(payload.get("amount").toString());

            Deposit deposit = depositService.makeDeposit(accountId, amount);
            return ResponseEntity.ok(deposit);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }
}
