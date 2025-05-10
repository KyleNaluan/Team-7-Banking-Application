package com.team7.bankingapp.repository;

import com.team7.bankingapp.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByUsername(String username);

    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByUsernameOrEmail(String username, String email);
}
