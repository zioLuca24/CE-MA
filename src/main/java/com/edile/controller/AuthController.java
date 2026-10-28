package com.edile.controller;

import com.edile.service.UtenteAppService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UtenteAppService utenteService;

    @PostMapping("/login")
    public ResponseEntity<UtenteAppService.LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(utenteService.login(request.getUsername(), request.getPassword()));
    }

    @GetMapping("/validate")
    public ResponseEntity<ValidateResponse> validate(@RequestHeader(value = "Authorization", required = false) String authorization) {
        String token = extractBearer(authorization);
        return ResponseEntity.ok(new ValidateResponse(utenteService.isTokenValido(token)));
    }

    private String extractBearer(String authorization) {
        if (authorization == null) {
            return null;
        }
        String prefix = "Bearer ";
        return authorization.startsWith(prefix) ? authorization.substring(prefix.length()) : authorization;
    }

    @Data
    private static class LoginRequest {
        private String username;
        private String password;
    }

    @Data
    private static class ValidateResponse {
        private final boolean valid;
    }
}
