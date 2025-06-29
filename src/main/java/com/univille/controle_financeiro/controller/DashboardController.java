package com.univille.controle_financeiro.controller;

import com.univille.controle_financeiro.dto.DashboardResponse;
import com.univille.controle_financeiro.dto.TransactionResponse;
import com.univille.controle_financeiro.entity.TransactionType;
import com.univille.controle_financeiro.entity.User;
import com.univille.controle_financeiro.service.TransactionService;
import com.univille.controle_financeiro.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {

        User user = getUserFromAuth(authentication);
        DashboardResponse dashboard = new DashboardResponse();

        // Se não especificado, usa o mês atual
        if (startDate == null || endDate == null) {
            LocalDate now = LocalDate.now();
            startDate = now.withDayOfMonth(1);
            endDate = now.withDayOfMonth(now.lengthOfMonth());
        }

        // Totais por tipo
        BigDecimal totalEntradas = transactionService.getTotalByTypeAndPeriod(user, TransactionType.ENTRADA, startDate, endDate);
        BigDecimal totalSaidas = transactionService.getTotalByTypeAndPeriod(user, TransactionType.SAIDA, startDate, endDate);

        dashboard.setTotalEntradas(totalEntradas);
        dashboard.setTotalSaidas(totalSaidas);
        dashboard.setSaldo(totalEntradas.subtract(totalSaidas));

        // Por categoria
        Map<String, BigDecimal> entradasPorCategoria = transactionService.getTotalByTypeAndPeriodGroupByCategory(
                user, TransactionType.ENTRADA, startDate, endDate);
        Map<String, BigDecimal> saidasPorCategoria = transactionService.getTotalByTypeAndPeriodGroupByCategory(
                user, TransactionType.SAIDA, startDate, endDate);

        dashboard.setEntradasPorCategoria(entradasPorCategoria);
        dashboard.setSaidasPorCategoria(saidasPorCategoria);

        // Transações recentes (últimas 10)
        List<TransactionResponse> recentTransactions = transactionService.findByUserAndPeriod(user, startDate, endDate)
                .stream()
                .limit(10)
                .toList();
        dashboard.setRecentTransactions(recentTransactions);

        return ResponseEntity.ok(dashboard);
    }

    private User getUserFromAuth(Authentication authentication) {
        String email = authentication.getName();
        return userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }
}