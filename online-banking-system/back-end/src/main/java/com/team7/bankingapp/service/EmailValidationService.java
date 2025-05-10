package com.team7.bankingapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@Service
public class EmailValidationService {

    @Value("${mailboxlayer.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public boolean isValidEmail(String email) {
        String url = "http://apilayer.net/api/check?access_key=" + apiKey +
                "&email=" + email +
                "&smtp=1&format=1";

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map body = response.getBody();

            return body != null &&
                    Boolean.TRUE.equals(body.get("format_valid")) &&
                    Boolean.TRUE.equals(body.get("smtp_check")) &&
                    Boolean.FALSE.equals(body.get("disposable"));

        } catch (Exception e) {
            return false;
        }
    }
}
