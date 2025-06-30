package com.univille.controle_financeiro.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest req) {
        return req.getRequestURI();
    }

    @ModelAttribute("pageTitle")
    public String pageTitle(HttpServletRequest req) {
        return switch (req.getRequestURI()) {
            case "/login"    -> "Entrar";
            case "/register" -> "Cadastrar";
            case "/dashboard"-> "Dashboard";
            case "/transactions" -> "Transações";
            case "/categories"   -> "Categorias";
            default -> "FinanceControl";
        };
    }

    @ModelAttribute("pageSubtitle")
    public String pageSubtitle(HttpServletRequest req) {
        return switch (req.getRequestURI()) {
            case "/login", "/register" -> "Acesse sua conta ou crie uma nova";
            case "/dashboard"            -> "Visão geral das suas finanças";
            default -> "";
        };
    }

    @ModelAttribute("breadcrumbs")
    public List<Map<String, String>> breadcrumbs(HttpServletRequest req,
                                                 @ModelAttribute("pageTitle") String pageTitle) {
        String uri = req.getRequestURI();
        List<Map<String,String>> crumbs = new ArrayList<>();
        crumbs.add(Map.of("title","Home","url","/dashboard"));
        if (!"/dashboard".equals(uri)) {
            crumbs.add(Map.of("title", pageTitle, "url", uri));
        }
        return crumbs;
    }

    @ModelAttribute("successMessages")
    public List<String> successMessages() { return List.of(); }

    @ModelAttribute("errorMessages")
    public List<String> errorMessages()   { return List.of(); }
}