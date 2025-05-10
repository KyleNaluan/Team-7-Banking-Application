package com.team7.bankingapp.repository;

import com.team7.bankingapp.model.Transfer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, Integer> {
    List<Transfer> findBySenderAccountID(long senderId);

    List<Transfer> findByReceiverAccountID(long receiverId);
}