package com.team7.bankingapp.controller;

import com.team7.bankingapp.model.Deposit;
import com.team7.bankingapp.model.Payment;
import com.team7.bankingapp.model.Transfer;
import com.team7.bankingapp.model.Withdrawal;
import com.team7.bankingapp.repository.DepositRepository;
import com.team7.bankingapp.repository.PaymentRepository;
import com.team7.bankingapp.repository.TransferRepository;
import com.team7.bankingapp.repository.WithdrawalRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private DepositRepository depositRepo;
    @Autowired
    private WithdrawalRepository withdrawalRepo;
    @Autowired
    private PaymentRepository paymentRepo;
    @Autowired
    private TransferRepository transferRepo;

    @GetMapping("/account/{accountId}")
    public List<Map<String, Object>> getAllTransactionsForAccount(@PathVariable long accountId) {
        List<Map<String, Object>> transactions = new ArrayList<>();

        List<Deposit> deposits = depositRepo.findByAccountAccountID(accountId);
        List<Withdrawal> withdrawals = withdrawalRepo.findByAccountAccountID(accountId);
        List<Payment> payments = paymentRepo.findByAccountAccountID(accountId);
        List<Transfer> sentTransfers = transferRepo.findBySenderAccountID(accountId);
        List<Transfer> receivedTransfers = transferRepo.findByReceiverAccountID(accountId);

        for (Deposit d : deposits) {
            transactions.add(Map.of(
                    "type", "Deposit",
                    "amount", d.getAmount(),
                    "transactionDate", d.getTransactionDate(),
                    "transactionTime", d.getTransactionTime(),
                    "note", ""));
        }

        for (Withdrawal w : withdrawals) {
            transactions.add(Map.of(
                    "type", "Withdrawal",
                    "amount", w.getAmount(),
                    "transactionDate", w.getTransactionDate(),
                    "transactionTime", w.getTransactionTime(),
                    "note", ""));
        }

        for (Payment p : payments) {
            transactions.add(Map.of(
                    "type", "Payment",
                    "amount", p.getAmount(),
                    "transactionDate", p.getTransactionDate(),
                    "transactionTime", p.getTransactionTime(),
                    "note", p.getComment() != null ? p.getComment() : "",
                    "category", p.getCategory().getCategoryName(),
                    "paymentId", p.getTransactionID()));
        }

        for (Transfer t : sentTransfers) {
            transactions.add(Map.of(
                    "type", "Transfer Sent",
                    "amount", t.getAmount(),
                    "transactionDate", t.getTransactionDate(),
                    "transactionTime", t.getTransactionTime(),
                    "note", t.getComment() != null ? t.getComment() : ""));
        }

        for (Transfer t : receivedTransfers) {
            transactions.add(Map.of(
                    "type", "Transfer Received",
                    "amount", t.getAmount(),
                    "transactionDate", t.getTransactionDate(),
                    "transactionTime", t.getTransactionTime(),
                    "note", t.getComment() != null ? t.getComment() : ""));
        }

        return transactions.stream()
                .sorted((a, b) -> {
                    String dateA = a.get("transactionDate").toString() + " " + a.get("transactionTime").toString();
                    String dateB = b.get("transactionDate").toString() + " " + b.get("transactionTime").toString();
                    return dateB.compareTo(dateA);
                })
                .collect(Collectors.toList());
    }

}
