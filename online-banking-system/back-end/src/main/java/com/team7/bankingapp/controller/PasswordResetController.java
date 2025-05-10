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
public class PasswordResetController {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private EmailSenderService emailSenderService;

    @Autowired
    private CustomerService customerService;

    @PostMapping("/request-password-reset")
    public ResponseEntity<String> requestPasswordReset(@RequestParam String email) {
        Optional<Customer> customer = customerService.findByEmail(email);
        if (customer.isEmpty()) {
            return ResponseEntity.badRequest().body("No account found with that email.");
        }

        String token = tokenService.generateToken(email);
        try {
            emailSenderService.sendPasswordResetEmail(email, token);
            return ResponseEntity.ok("Password reset email sent.");
        } catch (MessagingException e) {
            return ResponseEntity.internalServerError().body("Failed to send email.");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword,
            HttpSession session,
            HttpServletResponse response) {
        String email = tokenService.validateToken(token);
        if (email == null) {
            return ResponseEntity.badRequest().body("Invalid or expired token.");
        }

        String passwordError = customerService.validatePassword(newPassword);
        if (passwordError != null) {
            return ResponseEntity.badRequest().body(passwordError);
        }

        boolean updated = customerService.updatePasswordByEmail(email, newPassword);
        if (updated) {
            tokenService.invalidateToken(token);

            SecurityContextHolder.clearContext();
            session.invalidate();

            Cookie cookie = new Cookie("MY_SESSION_COOKIE", null);
            cookie.setMaxAge(0);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setAttribute("SameSite", "Lax");
            response.addCookie(cookie);

            return ResponseEntity.ok("Password updated successfully.");
        } else {
            return ResponseEntity.internalServerError().body("Failed to update password.");
        }
    }

    @GetMapping("/validate-token")
    public ResponseEntity<Void> validateToken(@RequestParam String token) {
        String email = tokenService.validateToken(token);
        return email != null ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

}
