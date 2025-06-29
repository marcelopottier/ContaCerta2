package com.univille.controle_financeiro.controller;

import com.univille.controle_financeiro.dto.AuthResponse;
import com.univille.controle_financeiro.dto.LoginRequest;
import com.univille.controle_financeiro.dto.RegisterRequest;
import com.univille.controle_financeiro.entity.User;
import com.univille.controle_financeiro.security.JwtUtil;
import com.univille.controle_financeiro.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = userService.createUser(request.getName(), request.getEmail(), request.getPassword());
            String token = jwtUtil.generateToken(user.getEmail());

            AuthResponse response = new AuthResponse(token, user.getEmail(), user.getName());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            User user = userService.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Credenciais inválidas"));

            if (!userService.checkPassword(user, request.getPassword())) {
                throw new RuntimeException("Credenciais inválidas");
            }

            String token = jwtUtil.generateToken(user.getEmail());
            AuthResponse response = new AuthResponse(token, user.getEmail(), user.getName());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        }
    }
}