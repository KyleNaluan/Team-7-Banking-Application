package com.team7.bankingapp.controller;

import com.team7.bankingapp.model.Transfer;
import com.team7.bankingapp.service.TransferService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/transfer")
public class TransferController {

    @Autowired
    private TransferService transferService;

    @PostMapping
    public ResponseEntity<?> transferFunds(@RequestBody Map<String, Object> payload) {
        try {
            long sourceAccount = Long.parseLong(payload.get("sourceAccount").toString());
            long receivingAccount = Long.parseLong(payload.get("receivingAccount").toString());
            BigDecimal amount = new BigDecimal(payload.get("amount").toString());
            String comment = payload.getOrDefault("comment", "").toString();

            Transfer result = transferService.executeTransfer(sourceAccount, receivingAccount, amount, comment);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unexpected error occurred."));
        }
    }

}
