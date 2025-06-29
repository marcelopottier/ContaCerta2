package com.univille.controle_financeiro.controller;

import com.univille.controle_financeiro.dto.TransactionRequest;
import com.univille.controle_financeiro.dto.TransactionResponse;
import com.univille.controle_financeiro.entity.Category;
import com.univille.controle_financeiro.entity.Transaction;
import com.univille.controle_financeiro.entity.User;
import com.univille.controle_financeiro.service.CategoryService;
import com.univille.controle_financeiro.service.TransactionService;
import com.univille.controle_financeiro.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getTransactions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {

        User user = getUserFromAuth(authentication);
        List<TransactionResponse> transactions;

        if (startDate != null && endDate != null) {
            transactions = transactionService.findByUserAndPeriod(user, startDate, endDate);
        } else {
            transactions = transactionService.findByUser(user);
        }

        return ResponseEntity.ok(transactions);
    }

    @PostMapping
    public ResponseEntity<?> createTransaction(@Valid @RequestBody TransactionRequest request, Authentication authentication) {
        try {
            User user = getUserFromAuth(authentication);
            Category category = categoryService.findByIdAndUser(request.getCategoryId(), user)
                    .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

            Transaction transaction = transactionService.createTransaction(
                    request.getType(),
                    request.getValue(),
                    request.getDate(),
                    request.getDescription(),
                    user,
                    category
            );

            return ResponseEntity.ok(transaction);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTransaction(@PathVariable Long id, @Valid @RequestBody TransactionRequest request,
                                               Authentication authentication) {
        try {
            User user = getUserFromAuth(authentication);
            Category category = categoryService.findByIdAndUser(request.getCategoryId(), user)
                    .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

            Transaction transaction = transactionService.updateTransaction(
                    id,
                    request.getType(),
                    request.getValue(),
                    request.getDate(),
                    request.getDescription(),
                    user,
                    category
            );

            return ResponseEntity.ok(transaction);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTransaction(@PathVariable Long id, Authentication authentication) {
        try {
            User user = getUserFromAuth(authentication);
            transactionService.deleteTransaction(id, user);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        }
    }

    private User getUserFromAuth(Authentication authentication) {
        String email = authentication.getName();
        return userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }
}