package com.univille.controle_financeiro.controller;

import com.univille.controle_financeiro.entity.User;
import com.univille.controle_financeiro.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.format.DateTimeFormatter;


@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    /**
     * Exibe a página de perfil
     */
    @GetMapping
    public String showProfile(Authentication authentication, Model model) {
        try {
            User user = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            model.addAttribute("user", user);
            model.addAttribute("avatarFileName", user.getAvatarFileName());
            String formattedDate = null;
            if (user.getDateOfBirth() != null) {
                formattedDate = user.getDateOfBirth().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
            model.addAttribute("formattedDateOfBirth", formattedDate);
            return "profile";

        } catch (Exception e) {
            model.addAttribute("error", "Erro ao carregar perfil: " + e.getMessage());
            return "profile";
        }
    }

    /**
     * Atualiza informações pessoais
     */
    @PostMapping("/update")
    public String updateProfile(@Valid @ModelAttribute User userForm,
                                BindingResult result,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Por favor, corrija os erros no formulário");
            return "redirect:/profile";
        }

        try {
            User currentUser = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            // Atualizar apenas os campos permitidos
            currentUser.setName(userForm.getName());
            currentUser.setEmail(userForm.getEmail());
            currentUser.setPhone(userForm.getPhone());
            System.out.println("Data recebida: " + userForm.getDateOfBirth());
            currentUser.setDateOfBirth(userForm.getDateOfBirth());

            userService.updateUser(currentUser);

            redirectAttributes.addFlashAttribute("success", "Perfil atualizado com sucesso!");

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro interno do servidor");
        }

        return "redirect:/profile";
    }

    @PostMapping("/avatar")
    @ResponseBody
    public ResponseEntity<?> updateAvatar(
            @RequestParam("fileName") String fileName,
            Authentication authentication) {
        try {
            userService.updateAvatar(authentication.getName(), fileName);
            return ResponseEntity.ok().body("Avatar salvo com sucesso");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao salvar avatar: " + e.getMessage());
        }
    }

    /**
     * Altera a senha do usuário
     */
    @PostMapping("/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {

        // Validações básicas
        if (newPassword == null || newPassword.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Nova senha é obrigatória");
            return "redirect:/profile";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "As senhas não coincidem!");
            return "redirect:/profile";
        }

        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "A nova senha deve ter pelo menos 6 caracteres");
            return "redirect:/profile";
        }

        try {
            User user = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            userService.changePassword(user, currentPassword, newPassword);

            redirectAttributes.addFlashAttribute("success", "Senha alterada com sucesso!");

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro interno do servidor");
        }

        return "redirect:/profile";
    }

    /**
     * Endpoint AJAX para alterar senha (opcional)
     */
    @PostMapping("/api/change-password")
    @ResponseBody
    public String changePasswordAjax(@RequestParam String currentPassword,
                                     @RequestParam String newPassword,
                                     @RequestParam String confirmPassword,
                                     Authentication authentication) {
        try {
            // Validações
            if (!newPassword.equals(confirmPassword)) {
                return "As senhas não coincidem!";
            }

            if (newPassword.length() < 6) {
                return "A nova senha deve ter pelo menos 6 caracteres";
            }

            User user = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            userService.changePassword(user, currentPassword, newPassword);

            return "success";

        } catch (RuntimeException e) {
            return e.getMessage();
        } catch (Exception e) {
            return "Erro interno do servidor";
        }
    }
}