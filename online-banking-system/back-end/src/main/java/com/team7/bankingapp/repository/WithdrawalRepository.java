package com.team7.bankingapp.repository;

import com.team7.bankingapp.model.Account;
import com.team7.bankingapp.model.Withdrawal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface WithdrawalRepository extends JpaRepository<Withdrawal, Integer> {
    List<Withdrawal> findByAccount(Account account);

    List<Withdrawal> findByAccountAccountID(long accountId);
}