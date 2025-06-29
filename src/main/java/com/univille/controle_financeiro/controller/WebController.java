package com.univille.controle_financeiro.controller;

import com.univille.controle_financeiro.entity.User;
import com.univille.controle_financeiro.service.CategoryService;
import com.univille.controle_financeiro.service.TransactionService;
import com.univille.controle_financeiro.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
     * Página inicial
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }

    /**
     * Página de login
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
     * Página de registro
     */
    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    /**
     * Processar registro
     */
    @PostMapping("/register")
    public String processRegister(@ModelAttribute("user") User user,
                                  @RequestParam("password") String password,
                                  @RequestParam("confirmPassword") String confirmPassword,
                                  Model model, RedirectAttributes redirectAttributes) {
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

            // Criar usuário
            userService.createUser(user.getName(), user.getEmail(), password);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Conta criada com sucesso! Faça login para continuar.");
            return "redirect:/login";

        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "register";
        }
    }

    /**
     * Dashboard principal
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);

            // Carregar dados do dashboard
            Map<String, Object> dashboardData = getDashboardData(user);
            model.addAttribute("dashboardData", dashboardData);

            // Carregar categorias para o modal de transação
            model.addAttribute("categories", categoryService.findByUser(user));

            // Dados do usuário
            model.addAttribute("userName", user.getName());

            return "dashboard";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Erro ao carregar dashboard: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Página de categorias
     */
    @GetMapping("/categories")
    public String categories(Model model, Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);

            // Carregar categorias do usuário
            model.addAttribute("categories", categoryService.findByUser(user));

            // Estatísticas das categorias
            Map<String, Object> stats = getCategoriesStats(user);
            model.addAttribute("categoriesStats", stats);

            // Dados do usuário
            model.addAttribute("userName", user.getName());

            return "categories";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Erro ao carregar categorias: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Página de transações
     */
    @GetMapping("/transactions")
    public String transactions(@RequestParam(required = false) LocalDate startDate,
                               @RequestParam(required = false) LocalDate endDate,
                               @RequestParam(required = false) Long categoryId,
                               @RequestParam(required = false) String type,
                               @RequestParam(required = false) String search,
                               @RequestParam(defaultValue = "date") String sort,
                               @RequestParam(defaultValue = "desc") String direction,
                               @PageableDefault(size = 20) Pageable pageable,
                               Model model, Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);

            // Aplicar filtros padrão se não especificados
            if (startDate == null && endDate == null) {
                LocalDate now = LocalDate.now();
                startDate = now.withDayOfMonth(1); // Primeiro dia do mês
                endDate = now.withDayOfMonth(now.lengthOfMonth()); // Último dia do mês
            }

            /* Carregar transações com filtros
            var transactions = transactionService.findTransactionsWithFilters(
                    user, startDate, endDate, categoryId, type, search, pageable);

            model.addAttribute("transactions", transactions);
             */
            // Estatísticas das transações
            Map<String, Object> stats = getTransactionStats(user, startDate, endDate);
            model.addAttribute("transactionStats", stats);

            // Carregar categorias para filtros
            model.addAttribute("categories", categoryService.findByUser(user));

            // Parâmetros de filtro para manter no formulário
            model.addAttribute("param", Map.of(
                    "startDate", startDate != null ? startDate.toString() : "",
                    "endDate", endDate != null ? endDate.toString() : "",
                    "categoryId", categoryId != null ? categoryId.toString() : "",
                    "type", type != null ? type : "",
                    "search", search != null ? search : ""
            ));

            // Parâmetros de ordenação
            model.addAttribute("sortField", sort);
            model.addAttribute("sortDirection", direction);

            // Dados do usuário
            model.addAttribute("userName", user.getName());

            return "transactions";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Erro ao carregar transações: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Página de perfil
     */
    @GetMapping("/profile")
    public String profile(Model model, Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);

            model.addAttribute("user", user);

            // Estatísticas do usuário
            Map<String, Object> userStats = getUserStats(user);
            model.addAttribute("userStats", userStats);

            // Dados do usuário
            model.addAttribute("userName", user.getName());

            return "profile";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Erro ao carregar perfil: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Atualizar perfil
     */
    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute User user,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            User currentUser = getCurrentUser(authentication);

            // Atualizar apenas campos permitidos
            currentUser.setName(user.getName());
            currentUser.setEmail(user.getEmail());

            userService.updateUser(currentUser);

            redirectAttributes.addFlashAttribute("successMessage", "Perfil atualizado com sucesso!");
            return "redirect:/profile";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao atualizar perfil: " + e.getMessage());
            return "redirect:/profile";
        }
    }

    /**
     * Página de erro personalizada
     */
    @GetMapping("/error")
    public String error() {
        return "error";
    }

    // ========== MÉTODOS AUXILIARES ==========

    /**
     * Obter usuário atual da autenticação
     */
    private User getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    /**
     * Obter dados do dashboard
     */
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

        return data;
    }

    /**
     * Obter estatísticas das categorias
     */
    private Map<String, Object> getCategoriesStats(User user) {
        Map<String, Object> stats = new HashMap<>();

        var categories = categoryService.findByUser(user);
        stats.put("totalCategorias", categories.size());
        stats.put("categoriasComTransacoes", categories.stream()
                .mapToInt(c -> c.getTransactions() != null ? c.getTransactions().size() : 0)
                .sum());

        // Categoria mais usada
        String categoriaMaisUsada = categories.stream()
                .filter(c -> c.getTransactions() != null && !c.getTransactions().isEmpty())
                .max((c1, c2) -> Integer.compare(
                        c1.getTransactions().size(),
                        c2.getTransactions().size()))
                .map(c -> c.getName())
                .orElse("N/A");

        stats.put("maiorCategoria", categoriaMaisUsada);
        stats.put("totalTransacoes", categories.stream()
                .mapToInt(c -> c.getTransactions() != null ? c.getTransactions().size() : 0)
                .sum());

        return stats;
    }

    /**
     * Obter estatísticas das transações
     */
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

        // Contar transações
        var transactions = transactionService.findByUserAndPeriod(user, startDate, endDate);
        stats.put("totalTransacoes", transactions.size());

        return stats;
    }

    /**
     * Obter estatísticas do usuário
     */
    private Map<String, Object> getUserStats(User user) {
        Map<String, Object> stats = new HashMap<>();

        // Total de transações
        var allTransactions = transactionService.findByUser(user);
        stats.put("totalTransacoes", allTransactions.size());

        // Total de categorias
        var categories = categoryService.findByUser(user);
        stats.put("totalCategorias", categories.size());

        // Transações este mês
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        var monthTransactions = transactionService.findByUserAndPeriod(user, startOfMonth, endOfMonth);
        stats.put("transacoesMes", monthTransactions.size());

        // Categoria mais usada
        String categoriaMaisUsada = categories.stream()
                .filter(c -> c.getTransactions() != null && !c.getTransactions().isEmpty())
                .max((c1, c2) -> Integer.compare(
                        c1.getTransactions().size(),
                        c2.getTransactions().size()))
                .map(c -> c.getName())
                .orElse("N/A");

        stats.put("categoriaMaisUsada", categoriaMaisUsada);
        stats.put("ultimoAcesso", LocalDate.now());
        stats.put("diasAtivo", 30); // Placeholder

        return stats;
    }
}