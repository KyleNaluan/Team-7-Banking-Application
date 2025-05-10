package com.team7.bankingapp.service;

import com.team7.bankingapp.model.Customer;
import com.team7.bankingapp.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepo;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private EmailValidationService emailValidationService;

    public String registerUser(Customer customer) {
        if (customerRepo.findByUsername(customer.getUsername()).isPresent()) {
            return "Username already exists!";
        }

        if (customerRepo.findByEmail(customer.getEmail()).isPresent()) {
            return "Email already registered!";
        }

        if (!emailValidationService.isValidEmail(customer.getEmail())) {
            return "Invalid or undeliverable email address.";
        }

        customer.setCustomerID(generateUniqueID());
        customer.setPassword(passwordEncoder.encode(customer.getPassword()));

        customerRepo.save(customer);
        return "User registered successfully!";
    }

    private Long generateUniqueID() {
        Random random = new Random();
        long id;
        do {
            id = 1000000000L + (long) (random.nextDouble() * 9000000000L);
        } while (customerRepo.existsById(id));
        return id;
    }

    public Optional<Customer> authenticateUser(String identifier, String password) {
        Optional<Customer> customer = customerRepo.findByUsernameOrEmail(identifier, identifier);
        if (customer.isPresent() && passwordEncoder.matches(password, customer.get().getPassword())) {
            return customer;
        }
        return Optional.empty();
    }

    public Optional<Customer> findByEmail(String email) {
        return customerRepo.findByEmail(email);
    }

    public String validateCustomerFields(Customer customer) {
        if (customer.getUsername() == null || !customer.getUsername().matches("^[a-zA-Z0-9]+$")) {
            return "Username must only contain letters and numbers.";
        }

        if (customer.getFName() == null || !customer.getFName().matches("^[a-zA-Z]+$")) {
            return "First name must only contain letters.";
        }

        if (customer.getLName() == null || !customer.getLName().matches("^[a-zA-Z]+$")) {
            return "Last name must only contain letters.";
        }

        if (customer.getPhoneNo() != null && !customer.getPhoneNo().matches("^\\d{10}$")) {
            return "Phone number must be 10 digits (US only).";
        }

        if (customer.getDateOfBirth() == null ||
                customer.getDateOfBirth().plusYears(16).isAfter(java.time.LocalDate.now())) {
            return "You must be at least 16 years old.";
        }

        return null;
    }

    public String validatePassword(String password) {
        if (password == null || password.length() < 8) {
            return "Password must be at least 8 characters.";
        }
        if (!password.matches(".*[A-Z].*")) {
            return "Password must include at least one uppercase letter.";
        }
        if (!password.matches(".*\\d.*")) {
            return "Password must include at least one number.";
        }
        if (!password.matches(".*[@$!%*?&].*")) {
            return "Password must include at least one special character.";
        }
        return null;
    }

    public boolean updatePasswordByEmail(String email, String newPassword) {
        Optional<Customer> optionalCustomer = customerRepo.findByEmail(email);
        if (optionalCustomer.isPresent()) {
            Customer customer = optionalCustomer.get();
            customer.setPassword(passwordEncoder.encode(newPassword));
            customerRepo.save(customer);
            return true;
        }
        return false;
    }

    public String updateEmail(String oldEmail, String newEmail) {
        if (!emailValidationService.isValidEmail(newEmail)) {
            return "Invalid or undeliverable email address.";
        }

        Optional<Customer> optionalCustomer = customerRepo.findByEmail(oldEmail);
        if (optionalCustomer.isEmpty()) {
            return "Original email not found.";
        }

        Customer customer = optionalCustomer.get();

        Optional<Customer> existing = customerRepo.findByEmail(newEmail);
        if (existing.isPresent() && !existing.get().getCustomerID().equals(customer.getCustomerID())) {
            return "Email already registered!";
        }

        customer.setEmail(newEmail);
        customerRepo.saveAndFlush(customer);
        return "Email updated successfully!";
    }

}
