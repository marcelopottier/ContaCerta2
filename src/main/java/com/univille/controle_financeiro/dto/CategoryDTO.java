package com.univille.controle_financeiro.dto;

import com.univille.controle_financeiro.entity.Category;

public record CategoryDTO(Long id, String name) {
    public static CategoryDTO fromEntity(Category category) {
        return new CategoryDTO(category.getId(), category.getName());
    }
}