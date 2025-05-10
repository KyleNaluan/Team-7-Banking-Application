package com.team7.bankingapp.repository;

import com.team7.bankingapp.model.Account;
import com.team7.bankingapp.model.Deposit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface DepositRepository extends JpaRepository<Deposit, Integer> {
    List<Deposit> findByAccount(Account account);

    List<Deposit> findByAccountAccountID(long accountId);
}
