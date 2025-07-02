package com.univille.controle_financeiro.dto;

import com.univille.controle_financeiro.entity.Category;

public class CategoryDTO {
    private Long id;
    private String name;
    private String color;
    private int transactionCount;

    // Constructors
    public CategoryDTO() {}

    public CategoryDTO(Long id, String name, String color, int transactionCount) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.transactionCount = transactionCount;
    }

    // Static method para converter da entidade
    public static CategoryDTO fromEntity(Category category) {
        return new CategoryDTO(
                category.getId(),
                category.getName(),
                category.getColor(),
                category.getTransactions() != null ? category.getTransactions().size() : 0
        );
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public int getTransactionCount() { return transactionCount; }
    public void setTransactionCount(int transactionCount) { this.transactionCount = transactionCount; }
}