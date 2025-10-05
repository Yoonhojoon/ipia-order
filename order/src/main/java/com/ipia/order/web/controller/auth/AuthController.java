package com.ipia.order.web.controller.auth;

import com.ipia.order.web.dto.request.auth.LoginRequest;
import com.ipia.order.web.dto.request.auth.TokenRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @PostMapping("/login")
    public ResponseEntity<Void> login(@Validated LoginRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Validated TokenRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}


