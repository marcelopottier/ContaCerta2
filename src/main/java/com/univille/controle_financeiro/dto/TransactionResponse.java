package com.univille.controle_financeiro.dto;

import com.univille.controle_financeiro.entity.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionResponse {
    private Long id;
    private TransactionType type;
    private BigDecimal value;
    private LocalDate date;
    private String description;
    private String categoryName;

    // Constructors
    public TransactionResponse() {}

    public TransactionResponse(Long id, TransactionType type, BigDecimal value, LocalDate date, String description, String categoryName) {
        this.id = id;
        this.type = type;
        this.value = value;
        this.date = date;
        this.description = description;
        this.categoryName = categoryName;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
}