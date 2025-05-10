package com.team7.bankingapp.service;

import com.team7.bankingapp.model.Account;
import com.team7.bankingapp.model.Transfer;
import com.team7.bankingapp.repository.AccountRepository;
import com.team7.bankingapp.repository.TransferRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class TransferService {
    @Autowired
    private AccountRepository accountRepo;
    @Autowired
    private TransferRepository transferRepo;

    public Transfer executeTransfer(long sourceAccountId, long receivingAccountId, BigDecimal amount, String comment) {
        if (sourceAccountId == receivingAccountId) {
            throw new IllegalArgumentException("Source and receiving account cannot be the same.");
        }

        Account source = accountRepo.findById(sourceAccountId)
                .orElseThrow(() -> new RuntimeException("Source account not found"));
        Account receiver = accountRepo.findById(receivingAccountId)
                .orElseThrow(() -> new RuntimeException("Receiving account not found"));

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive.");
        }

        if (source.getAccountBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance for transfer.");
        }

        source.setAccountBalance(source.getAccountBalance().subtract(amount));
        receiver.setAccountBalance(receiver.getAccountBalance().add(amount));
        accountRepo.save(source);
        accountRepo.save(receiver);

        Transfer transfer = new Transfer();
        transfer.setAmount(amount);
        transfer.setTransactionDate(LocalDate.now());
        transfer.setTransactionTime(LocalTime.now());
        transfer.setSender(source);
        transfer.setReceiver(receiver);
        transfer.setComment(comment);
        return transferRepo.save(transfer);
    }
}
