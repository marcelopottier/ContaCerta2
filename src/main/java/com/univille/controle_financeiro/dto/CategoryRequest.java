package com.univille.controle_financeiro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CategoryRequest {
    @NotBlank(message = "Nome da categoria é obrigatório")
    @Size(max = 50)
    private String name;

    // Constructors
    public CategoryRequest() {}

    public CategoryRequest(String name) {
        this.name = name;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}