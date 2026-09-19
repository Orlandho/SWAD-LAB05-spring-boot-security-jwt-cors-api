package com.ejemplo.laboratorio05.controller;

import com.ejemplo.laboratorio05.dto.AuthResponse;
import com.ejemplo.laboratorio05.dto.LoginRequest;
import com.ejemplo.laboratorio05.security.TokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final long expiracion;

    public AuthController(AuthenticationManager authenticationManager,
                          TokenService tokenService,
                          @Value("${app.jwt.expiration:3600}") long expiracion) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.expiracion = expiracion;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(), request.password()));

        String token = tokenService.generarToken(authentication);
        return ResponseEntity.ok(new AuthResponse(token, "Bearer", expiracion));
    }
}
