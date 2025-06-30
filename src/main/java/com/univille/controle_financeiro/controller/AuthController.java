package com.univille.controle_financeiro.controller;

import com.univille.controle_financeiro.dto.AuthResponse;
import com.univille.controle_financeiro.dto.LoginRequest;
import com.univille.controle_financeiro.dto.RegisterRequest;
import com.univille.controle_financeiro.entity.User;
import com.univille.controle_financeiro.security.JwtUtil;
import com.univille.controle_financeiro.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
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
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request,
                                      HttpServletResponse response) {
        try {
            User user = userService.createUser(request.getName(), request.getEmail(), request.getPassword());
            String token = jwtUtil.generateToken(user.getEmail());

            // Definir cookie para páginas web
            setAuthCookie(response, token);

            AuthResponse authResponse = new AuthResponse(token, user.getEmail(), user.getName());
            return ResponseEntity.ok(authResponse);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Erro: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request,
                                   HttpServletResponse response) {
        try {
            User user = userService.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Credenciais inválidas"));

            if (!userService.checkPassword(user, request.getPassword())) {
                throw new RuntimeException("Credenciais inválidas");
            }

            String token = jwtUtil.generateToken(user.getEmail());

            // Definir cookie para páginas web
            setAuthCookie(response, token);

            AuthResponse authResponse = new AuthResponse(token, user.getEmail(), user.getName());
            return ResponseEntity.ok(authResponse);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Erro: " + e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // Limpar cookie
        clearAuthCookie(response);
        return ResponseEntity.ok().body(new MessageResponse("Logout realizado com sucesso"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7); // Remove "Bearer "
            String email = jwtUtil.getEmailFromToken(token);

            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            AuthResponse authResponse = new AuthResponse(token, user.getEmail(), user.getName());
            return ResponseEntity.ok(authResponse);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Token inválido"));
        }
    }

    private void setAuthCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("authToken", token);
        cookie.setHttpOnly(true); // Segurança: não acessível via JavaScript
        cookie.setSecure(false); // Defina como true em produção com HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60); // 24 horas
        response.addCookie(cookie);
    }

    private void clearAuthCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("authToken", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0); // Remove imediatamente
        response.addCookie(cookie);
    }

    // Classes auxiliares para respostas
    static class ErrorResponse {
        private String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }

    static class MessageResponse {
        private String message;

        public MessageResponse(String message) {
            this.message = message;
        }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}