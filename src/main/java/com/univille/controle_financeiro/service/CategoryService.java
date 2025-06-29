package com.univille.controle_financeiro.service;

import com.univille.controle_financeiro.entity.Category;
import com.univille.controle_financeiro.entity.User;
import com.univille.controle_financeiro.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public List<Category> findByUser(User user) {
        return categoryRepository.findByUserOrderByNameAsc(user);
    }

    public Category createCategory(String name, User user) {
        Category category = new Category();
        category.setName(name);
        category.setUser(user);
        return categoryRepository.save(category);
    }

    public Category updateCategory(Long id, String name, User user) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        if (!category.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Acesso negado");
        }

        category.setName(name);
        return categoryRepository.save(category);
    }

    public void deleteCategory(Long id, User user) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        if (!category.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Acesso negado");
        }

        categoryRepository.delete(category);
    }

    public Optional<Category> findByIdAndUser(Long id, User user) {
        Optional<Category> category = categoryRepository.findById(id);
        if (category.isPresent() && category.get().getUser().getId().equals(user.getId())) {
            return category;
        }
        return Optional.empty();
    }
}