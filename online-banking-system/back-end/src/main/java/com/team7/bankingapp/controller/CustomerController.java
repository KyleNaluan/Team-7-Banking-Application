package com.team7.bankingapp.controller;

import com.team7.bankingapp.model.Customer;
import com.team7.bankingapp.repository.CustomerRepository;
import com.team7.bankingapp.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerService customerService;

    @GetMapping("/all")
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @GetMapping("/{id}")
    public Optional<Customer> getCustomerById(@PathVariable Long id) {
        return customerRepository.findById(id);
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody Customer customer) {
        String validationError = customerService.validateCustomerFields(customer);
        if (validationError != null) {
            return ResponseEntity.badRequest().body(validationError);
        }

        if (customerRepository.findByUsername(customer.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists!");
        }

        if (customerRepository.findByEmail(customer.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already registered!");
        }

        customerRepository.save(customer);
        return ResponseEntity.ok("User registered successfully!");
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateCustomer(@PathVariable Long id, @RequestBody Customer updatedCustomer,
            HttpSession session) {
        Optional<Customer> existingCustomerOpt = customerRepository.findById(id);
        if (existingCustomerOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Customer not found.");
        }

        String validationError = customerService.validateCustomerFields(updatedCustomer);
        if (validationError != null) {
            return ResponseEntity.badRequest().body(validationError);
        }

        Customer existingCustomer = existingCustomerOpt.get();

        Optional<Customer> userWithSameUsername = customerRepository.findByUsername(updatedCustomer.getUsername());
        if (userWithSameUsername.isPresent() && !userWithSameUsername.get().getCustomerID().equals(id)) {
            return ResponseEntity.badRequest().body("Username already exists.");
        }

        existingCustomer.setUsername(updatedCustomer.getUsername());
        existingCustomer.setFName(updatedCustomer.getFName());
        existingCustomer.setLName(updatedCustomer.getLName());
        existingCustomer.setAddress(updatedCustomer.getAddress());
        existingCustomer.setPhoneNo(updatedCustomer.getPhoneNo());
        existingCustomer.setDateOfBirth(updatedCustomer.getDateOfBirth());

        customerRepository.save(existingCustomer);

        session.setAttribute("customer", existingCustomer);

        return ResponseEntity.ok("Profile updated successfully!");
    }
}
