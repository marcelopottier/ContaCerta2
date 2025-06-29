package com.univille.controle_financeiro.dto;

import com.univille.controle_financeiro.entity.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionRequest {
    @NotNull(message = "Tipo de transação é obrigatório")
    private TransactionType type;

    @NotNull(message = "Valor é obrigatório")
    @Positive(message = "Valor deve ser positivo")
    private BigDecimal value;

    @NotNull(message = "Data é obrigatória")
    private LocalDate date;

    @Size(max = 255)
    private String description;

    @NotNull(message = "Categoria é obrigatória")
    private Long categoryId;

    // Constructors
    public TransactionRequest() {}

    public TransactionRequest(TransactionType type, BigDecimal value, LocalDate date, String description, Long categoryId) {
        this.type = type;
        this.value = value;
        this.date = date;
        this.description = description;
        this.categoryId = categoryId;
    }

    // Getters and Setters
    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
}