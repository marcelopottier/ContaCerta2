package com.univille.controle_financeiro.controller;

import com.univille.controle_financeiro.dto.CategoryDTO;
import com.univille.controle_financeiro.dto.CategoryRequest;
import com.univille.controle_financeiro.entity.Category;
import com.univille.controle_financeiro.entity.User;
import com.univille.controle_financeiro.service.CategoryService;
import com.univille.controle_financeiro.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserService userService;

    @GetMapping
    public List<CategoryDTO> getCategories(Authentication auth) {
        User user = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return categoryService.findByUser(user)
                .stream()
                .map(CategoryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<?> createCategory(@Valid @RequestBody CategoryRequest request, Authentication authentication) {
        try {
            User user = getUserFromAuth(authentication);
            Category category = categoryService.createCategory(
                    request.getName(),
                    request.getColor(),
                    user
            );
            return ResponseEntity.ok(CategoryDTO.fromEntity(category));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest request,
                                            Authentication authentication) {
        try {
            User user = getUserFromAuth(authentication);
            Category category = categoryService.updateCategory(
                    id,
                    request.getName(),
                    request.getColor(),
                    user
            );
            return ResponseEntity.ok(CategoryDTO.fromEntity(category));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id, Authentication authentication) {
        try {
            User user = getUserFromAuth(authentication);
            categoryService.deleteCategory(id, user);
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