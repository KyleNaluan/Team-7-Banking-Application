package com.team7.bankingapp.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "Customer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    @Id
    private Long customerID;

    @Column(name = "fname", nullable = false)
    @JsonProperty("fName")
    private String fName;

    @Column(name = "lname", nullable = false)
    @JsonProperty("lName")
    private String lName;

    private String address;

    @Column(name = "phoneno")
    private String phoneNo;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "dateofbirth", nullable = false)
    @JsonProperty("dateOfBirth")
    private LocalDate dateOfBirth;
}
