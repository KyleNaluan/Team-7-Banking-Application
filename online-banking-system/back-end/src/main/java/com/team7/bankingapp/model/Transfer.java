package com.team7.bankingapp.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Transfer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transfer extends Transaction {
    @Column(length = 600)
    private String comment;

    @ManyToOne
    @JoinColumn(name = "senderid", nullable = false)
    private Account sender;

    @ManyToOne
    @JoinColumn(name = "receiverid", nullable = false)
    private Account receiver;
}
