package com.team7.bankingapp.controller;

import com.team7.bankingapp.model.Withdrawal;
import com.team7.bankingapp.service.WithdrawalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/withdrawals")
public class WithdrawalController {

    @Autowired
    private WithdrawalService withdrawalService;

    @PostMapping
    public ResponseEntity<?> withdrawMoney(@RequestBody Map<String, Object> payload) {
        try {
            long accountId = Long.parseLong(payload.get("accountId").toString());
            BigDecimal amount = new BigDecimal(payload.get("amount").toString());

            Withdrawal withdrawal = withdrawalService.createWithdrawal(accountId, amount);
            return ResponseEntity.ok(withdrawal);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }
}
