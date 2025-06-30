package com.univille.controle_financeiro.controller;

import com.univille.controle_financeiro.entity.User;
import com.univille.controle_financeiro.service.CategoryService;
import com.univille.controle_financeiro.service.TransactionService;
import com.univille.controle_financeiro.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller responsável pelas páginas web da aplicação
 * Agora usa apenas JWT para autenticação
 */
@Controller
public class WebController {

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private TransactionService transactionService;

    /**
     * Página inicial - pública
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }

    /**
     * Página de login - pública
     */
    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Credenciais inválidas. Tente novamente.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "Logout realizado com sucesso.");
        }
        return "login";
    }

    /**
     * Página de registro - pública
     */
    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    /**
     * Processar registro via form (alternativa ao API)
     */
    @PostMapping("/register")
    public String processRegister(@ModelAttribute("user") User user,
                                  @RequestParam("password") String password,
                                  @RequestParam("confirmPassword") String confirmPassword,
                                  Model model, RedirectAttributes redirectAttributes,
                                  HttpServletResponse response) {
        try {
            // Validação básica
            if (!password.equals(confirmPassword)) {
                model.addAttribute("errorMessage", "As senhas não coincidem.");
                return "register";
            }

            if (password.length() < 6) {
                model.addAttribute("errorMessage", "A senha deve ter pelo menos 6 caracteres.");
                return "register";
            }

            // Criar usuário via service
            User newUser = userService.createUser(user.getName(), user.getEmail(), password);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Conta criada com sucesso! Faça login para continuar.");
            return "redirect:/login";

        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "register";
        }
    }

    /**
     * Dashboard - requer autenticação JWT
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);

            // Carregar dados do dashboard
            Map<String, Object> dashboardData = getDashboardData(user);
            model.addAttribute("dashboardData", dashboardData);

            // Carregar categorias para o modal
            model.addAttribute("categories", categoryService.findByUser(user));

            // Dados do usuário
            model.addAttribute("userName", user.getName());
            model.addAttribute("userEmail", user.getEmail());

            return "dashboard";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Erro ao carregar dashboard: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Página de categorias - requer autenticação JWT
     */
    @GetMapping("/categories")
    public String categories(Model model, Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);

            model.addAttribute("categories", categoryService.findByUser(user));

            // Estatísticas das categorias
            Map<String, Object> stats = getCategoriesStats(user);
            model.addAttribute("categoriesStats", stats);

            model.addAttribute("userName", user.getName());

            return "categories";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Erro ao carregar categorias: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Página de transações - requer autenticação JWT
     */
    @GetMapping("/transactions")
    public String transactions(@RequestParam(required = false) LocalDate startDate,
                               @RequestParam(required = false) LocalDate endDate,
                               @RequestParam(required = false) Long categoryId,
                               @RequestParam(required = false) String type,
                               @RequestParam(required = false) String search,
                               Model model, Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);

            // Aplicar filtros padrão
            if (startDate == null && endDate == null) {
                LocalDate now = LocalDate.now();
                startDate = now.withDayOfMonth(1);
                endDate = now.withDayOfMonth(now.lengthOfMonth());
            }

            // Carregar transações (implementar filtros se necessário)
            var transactions = transactionService.findByUserAndPeriod(user, startDate, endDate);
            model.addAttribute("transactions", transactions);

            // Estatísticas das transações
            Map<String, Object> stats = getTransactionStats(user, startDate, endDate);
            model.addAttribute("transactionStats", stats);

            // Carregar categorias para filtros
            model.addAttribute("categories", categoryService.findByUser(user));

            // Parâmetros para manter nos filtros
            model.addAttribute("param", Map.of(
                    "startDate", startDate != null ? startDate.toString() : "",
                    "endDate", endDate != null ? endDate.toString() : "",
                    "categoryId", categoryId != null ? categoryId.toString() : "",
                    "type", type != null ? type : "",
                    "search", search != null ? search : ""
            ));

            model.addAttribute("userName", user.getName());

            return "transactions";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Erro ao carregar transações: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Página de perfil - requer autenticação JWT
     */
    @GetMapping("/profile")
    public String profile(Model model, Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);

            model.addAttribute("user", user);

            // Estatísticas do usuário
            Map<String, Object> userStats = getUserStats(user);
            model.addAttribute("userStats", userStats);

            model.addAttribute("userName", user.getName());

            return "profile";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Erro ao carregar perfil: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Logout - limpa cookies e redireciona
     */
    @GetMapping("/logout")
    public String logout(HttpServletResponse response, RedirectAttributes redirectAttributes) {
        // Limpar cookie de autenticação
        Cookie cookie = new Cookie("authToken", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        redirectAttributes.addFlashAttribute("successMessage", "Logout realizado com sucesso.");
        return "redirect:/login";
    }

    /**
     * Página de erro
     */
    @GetMapping("/error")
    public String error() {
        return "error";
    }


    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Usuário não autenticado");
        }

        String email = authentication.getName();
        return userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    private Map<String, Object> getDashboardData(User user) {
        Map<String, Object> data = new HashMap<>();

        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        // Totais do mês atual
        var totalEntradas = transactionService.getTotalByTypeAndPeriod(
                user, com.univille.controle_financeiro.entity.TransactionType.ENTRADA,
                startOfMonth, endOfMonth);
        var totalSaidas = transactionService.getTotalByTypeAndPeriod(
                user, com.univille.controle_financeiro.entity.TransactionType.SAIDA,
                startOfMonth, endOfMonth);

        data.put("totalEntradas", totalEntradas);
        data.put("totalSaidas", totalSaidas);
        data.put("saldo", totalEntradas.subtract(totalSaidas));

        // Totais por categoria
        data.put("entradasPorCategoria", transactionService.getTotalByTypeAndPeriodGroupByCategory(
                user, com.univille.controle_financeiro.entity.TransactionType.ENTRADA,
                startOfMonth, endOfMonth));
        data.put("saidasPorCategoria", transactionService.getTotalByTypeAndPeriodGroupByCategory(
                user, com.univille.controle_financeiro.entity.TransactionType.SAIDA,
                startOfMonth, endOfMonth));

        // Transações recentes
        var recentTransactions = transactionService.findByUserAndPeriod(user, startOfMonth, endOfMonth)
                .stream().limit(10).toList();
        data.put("recentTransactions", recentTransactions);

        return data;
    }

    private Map<String, Object> getCategoriesStats(User user) {
        Map<String, Object> stats = new HashMap<>();

        var categories = categoryService.findByUser(user);
        stats.put("totalCategorias", categories.size());

        return stats;
    }

    private Map<String, Object> getTransactionStats(User user, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> stats = new HashMap<>();

        var totalEntradas = transactionService.getTotalByTypeAndPeriod(
                user, com.univille.controle_financeiro.entity.TransactionType.ENTRADA,
                startDate, endDate);
        var totalSaidas = transactionService.getTotalByTypeAndPeriod(
                user, com.univille.controle_financeiro.entity.TransactionType.SAIDA,
                startDate, endDate);

        stats.put("totalEntradas", totalEntradas);
        stats.put("totalSaidas", totalSaidas);
        stats.put("saldo", totalEntradas.subtract(totalSaidas));

        var transactions = transactionService.findByUserAndPeriod(user, startDate, endDate);
        stats.put("totalTransacoes", transactions.size());

        return stats;
    }

    private Map<String, Object> getUserStats(User user) {
        Map<String, Object> stats = new HashMap<>();

        var allTransactions = transactionService.findByUser(user);
        stats.put("totalTransacoes", allTransactions.size());

        var categories = categoryService.findByUser(user);
        stats.put("totalCategorias", categories.size());

        return stats;
    }
}