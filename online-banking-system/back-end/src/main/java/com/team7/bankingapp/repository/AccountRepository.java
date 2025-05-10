package com.team7.bankingapp.repository;

import com.team7.bankingapp.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByCustomerCustomerID(long customerID);
}
