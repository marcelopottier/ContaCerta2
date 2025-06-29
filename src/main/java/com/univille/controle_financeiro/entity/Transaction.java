package com.univille.controle_financeiro.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Mapeia o ENUM para VARCHAR(10) em vez de criar ENUM no banco
    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    @NotNull(message = "Tipo de transação é obrigatório")
    private TransactionType type;

    // Renomeia a coluna para 'amount' para não usar palavra reservada
    @NotNull(message = "Valor é obrigatório")
    @Positive(message = "Valor deve ser positivo")
    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal value;

    @NotNull(message = "Data é obrigatória")
    @Column(nullable = false)
    private LocalDate date;

    @Size(max = 255)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    public Transaction() {}

    public Transaction(TransactionType type,
                       BigDecimal value,
                       LocalDate date,
                       String description,
                       User user,
                       Category category) {
        this.type        = type;
        this.value       = value;
        this.date        = date;
        this.description = description;
        this.user        = user;
        this.category    = category;
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

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
}