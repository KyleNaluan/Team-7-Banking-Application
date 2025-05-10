package com.team7.bankingapp.controller;

import com.team7.bankingapp.model.Customer;
import com.team7.bankingapp.service.CustomerService;
import com.team7.bankingapp.dto.LoginRequestDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.*;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import java.util.*;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private CustomerService customerService;

    @PostMapping("/register")
    public String registerUser(@RequestBody Customer customer) {
        System.out
                .println("Received: " + customer.getFName() + ", " + customer.getLName() + ", " + customer.getEmail());
        return customerService.registerUser(customer);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginUser(@RequestBody LoginRequestDto request,
            HttpSession session,
            HttpServletResponse response) {
        Optional<Customer> authenticatedUser = customerService.authenticateUser(request.getUsername(),
                request.getPassword());

        Map<String, String> res = new HashMap<>();
        if (authenticatedUser.isPresent()) {
            Customer customer = authenticatedUser.get();

            session.setAttribute("customer", customer);

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    customer, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authToken);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

            if (request.isRememberMe()) {
                response.setHeader("Set-Cookie", "MY_SESSION_COOKIE=" + session.getId()
                        + "; Max-Age=" + (60 * 60 * 24)
                        + "; Path=/; HttpOnly; Secure; SameSite=Lax");
            }

            res.put("message", "Login Successful!");
            return ResponseEntity.ok(res);
        } else {
            res.put("message", "Invalid Credentials");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logoutUser(HttpSession session, HttpServletResponse response) {
        session.invalidate();

        Cookie cookie = new Cookie("MY_SESSION_COOKIE", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setDomain("localhost");
        cookie.setAttribute("SameSite", "Lax");

        response.addCookie(cookie);

        return ResponseEntity.ok("Logout successful");
    }

    @GetMapping("/me")
    public ResponseEntity<Customer> getLoggedInUser(HttpSession session) {
        Customer user = (Customer) session.getAttribute("customer");
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

}
