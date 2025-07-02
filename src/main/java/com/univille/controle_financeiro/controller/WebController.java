package com.univille.controle_financeiro.controller;

import com.univille.controle_financeiro.entity.User;
import com.univille.controle_financeiro.service.CategoryService;
import com.univille.controle_financeiro.service.TransactionService;
import com.univille.controle_financeiro.service.UserService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @ModelAttribute
    public void addUserToModel(Model model, Authentication auth) {
        if (auth != null && auth.isAuthenticated()) {
            userService.findByEmail(auth.getName()).ifPresent(user -> {
                model.addAttribute("user", user);
                model.addAttribute("avatarFile", user.getAvatarFileName());
            });
        }
    }

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

    @GetMapping("/error")
    public String error(Model model) {
        return "login";
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

            if (startDate == null && endDate == null) {
                LocalDate now = LocalDate.now();
                startDate = now.withDayOfMonth(1);
                endDate = now.withDayOfMonth(now.lengthOfMonth());
            }

            var transactions = transactionService.findByUserAndPeriod(user, startDate, endDate);

            model.addAttribute("transactions", createMockPagedTransactions(transactions));


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


    @PostMapping("/categories")
    public String saveCategory(@RequestParam(required = false) Long id,
                               @RequestParam String name,
                               @RequestParam(required = false) String color,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(authentication);

        try {
            if (color == null || color.trim().isEmpty()) {
                color = "#6f42c1";
            }

            if (id != null) {
                // Edição
                categoryService.updateCategory(id, name, color, user);
                redirectAttributes.addFlashAttribute("successMessage", "Categoria atualizada com sucesso!");
            } else {
                // Criação
                categoryService.createCategory(name, color, user);
                redirectAttributes.addFlashAttribute("successMessage", "Categoria criada com sucesso!");
            }

            return "redirect:/categories";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/categories";
        }
    }

    // Métodos privados auxiliares

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

        // Buscar todas as transações do mês para contagem
        var allTransactions = transactionService.findByUserAndPeriod(user, startOfMonth, endOfMonth);
        data.put("totalTransacoes", allTransactions.size());

        // Totais por categoria
        var entradasPorCategoria = transactionService.getTotalByTypeAndPeriodGroupByCategory(
                user, com.univille.controle_financeiro.entity.TransactionType.ENTRADA,
                startOfMonth, endOfMonth);
        var saidasPorCategoria = transactionService.getTotalByTypeAndPeriodGroupByCategory(
                user, com.univille.controle_financeiro.entity.TransactionType.SAIDA,
                startOfMonth, endOfMonth);

        data.put("entradasPorCategoria", entradasPorCategoria);
        data.put("saidasPorCategoria", saidasPorCategoria);

        // Transações recentes (limitadas a 10)
        var recentTransactions = allTransactions.stream()
                .sorted((t1, t2) -> t2.getDate().compareTo(t1.getDate())) // Ordenar por data desc
                .limit(10)
                .toList();
        data.put("recentTransactions", recentTransactions);

        System.out.println("Dashboard Data:");
        System.out.println("- Total Transações: " + allTransactions.size());
        System.out.println("- Total Entradas: " + totalEntradas);
        System.out.println("- Total Saídas: " + totalSaidas);
        System.out.println("- Entradas por Categoria: " + entradasPorCategoria);
        System.out.println("- Saídas por Categoria: " + saidasPorCategoria);
        System.out.println("- Transações Recentes: " + recentTransactions.size());

        return data;
    }
    @GetMapping("/api/dashboard/chart-data")
    @ResponseBody
    public Map<String, Object> getChartData(@RequestParam(defaultValue = "month") String period,
                                            Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);

            LocalDate[] dateRange = calculateDateRange(period);
            LocalDate startDate = dateRange[0];
            LocalDate endDate = dateRange[1];

            System.out.println("Período: " + period + " | De: " + startDate + " | Até: " + endDate);

            var entradasPorCategoria = transactionService.getTotalByTypeAndPeriodGroupByCategory(
                    user, com.univille.controle_financeiro.entity.TransactionType.ENTRADA,
                    startDate, endDate);
            var saidasPorCategoria = transactionService.getTotalByTypeAndPeriodGroupByCategory(
                    user, com.univille.controle_financeiro.entity.TransactionType.SAIDA,
                    startDate, endDate);

            Map<String, Object> chartData = new HashMap<>();
            chartData.put("entradas", entradasPorCategoria);
            chartData.put("saidas", saidasPorCategoria);
            chartData.put("period", period);
            chartData.put("startDate", startDate.toString());
            chartData.put("endDate", endDate.toString());

            return chartData;

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("error", e.getMessage());
        }
    }

    private LocalDate[] calculateDateRange(String period) {
        LocalDate now = LocalDate.now();
        LocalDate startDate, endDate;

        switch (period.toLowerCase()) {
            case "quarter":
                startDate = now.minusMonths(2).withDayOfMonth(1);
                endDate = now.withDayOfMonth(now.lengthOfMonth());
                break;

            case "year":
                startDate = now.withDayOfYear(1);
                endDate = now.withDayOfYear(now.lengthOfYear());
                break;

            case "month":
            default:
                startDate = now.withDayOfMonth(1);
                endDate = now.withDayOfMonth(now.lengthOfMonth());
                break;
        }

        return new LocalDate[]{startDate, endDate};
    }

    private Map<String, Object> getCategoriesStats(User user) {
        Map<String, Object> stats = new HashMap<>();

        var categories = categoryService.findByUser(user);
        stats.put("totalCategorias", categories.size());
        stats.put("categoriasComTransacoes", categories.stream()
                .mapToInt(c -> c.getTransactions() != null ? c.getTransactions().size() : 0)
                .sum());
        stats.put("totalTransacoes", categories.stream()
                .mapToInt(c -> c.getTransactions() != null ? c.getTransactions().size() : 0)
                .sum());

        // Categoria mais usada
        var maiorCategoria = categories.stream()
                .filter(c -> c.getTransactions() != null && !c.getTransactions().isEmpty())
                .max((c1, c2) -> Integer.compare(c1.getTransactions().size(), c2.getTransactions().size()))
                .map(c -> c.getName())
                .orElse("N/A");
        stats.put("maiorCategoria", maiorCategoria);

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

        // Estatísticas adicionais
        stats.put("transacoesMes", allTransactions.size()); // Simplificado
        stats.put("categoriaMaisUsada", "Geral"); // Simplificado
        stats.put("ultimoAcesso", LocalDate.now());
        stats.put("diasAtivo", 30); // Simplificado

        return stats;
    }

    private Object createMockPagedTransactions(java.util.List<?> transactions) {
        return new Object() {
            public java.util.List<?> getContent() { return transactions; }
            public boolean isEmpty() { return transactions.isEmpty(); }
            public int getTotalElements() { return transactions.size(); }
            public int getTotalPages() { return 1; }
            public boolean isFirst() { return true; }
            public boolean isLast() { return true; }
            public int getNumber() { return 0; }
            public int getSize() { return transactions.size(); }
        };
    }
}