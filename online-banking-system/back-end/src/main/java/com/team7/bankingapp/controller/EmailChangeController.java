package com.team7.bankingapp.controller;

import com.team7.bankingapp.service.EmailSenderService;
import com.team7.bankingapp.service.TokenService;
import com.team7.bankingapp.service.CustomerService;
import com.team7.bankingapp.model.Customer;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class EmailChangeController {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private EmailSenderService emailSenderService;

    @Autowired
    private CustomerService customerService;

    @PostMapping("/request-email-change")
    public ResponseEntity<String> requestEmailChange(@RequestParam String currentEmail, @RequestParam String newEmail) {
        Optional<Customer> customer = customerService.findByEmail(currentEmail);
        if (customer.isEmpty()) {
            return ResponseEntity.badRequest().body("Current email not found.");
        }

        String token = tokenService.generateEmailChangeToken(currentEmail, newEmail);

        try {
            emailSenderService.sendEmailChangeEmail(currentEmail, token);
            return ResponseEntity.ok("Email change link sent.");
        } catch (MessagingException e) {
            return ResponseEntity.internalServerError().body("Failed to send email change link.");
        }
    }

    @PostMapping("/confirm-email-change")
    public ResponseEntity<String> confirmEmailChange(
            @RequestParam String token,
            @RequestParam String newEmail,
            HttpSession session,
            HttpServletResponse response) {
        TokenService.PendingEmailChange data = tokenService.validateEmailChangeToken(token);
        if (data == null) {
            return ResponseEntity.badRequest().body("Invalid or expired token.");
        }

        String result = customerService.updateEmail(data.currentEmail, newEmail);
        if (result.equals("Email updated successfully!")) {
            tokenService.invalidateEmailChangeToken(token);

            SecurityContextHolder.clearContext();
            session.invalidate();

            Cookie cookie = new Cookie("MY_SESSION_COOKIE", null);
            cookie.setMaxAge(0);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setAttribute("SameSite", "Lax");
            response.addCookie(cookie);

            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping("/validate-email-token")
    public ResponseEntity<Void> validateEmailToken(@RequestParam String token) {
        TokenService.PendingEmailChange data = tokenService.validateEmailChangeToken(token);
        return data != null ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

}
