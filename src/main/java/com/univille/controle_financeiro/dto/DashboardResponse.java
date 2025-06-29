package com.univille.controle_financeiro.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class DashboardResponse {
    private BigDecimal totalEntradas;
    private BigDecimal totalSaidas;
    private BigDecimal saldo;
    private Map<String, BigDecimal> entradasPorCategoria;
    private Map<String, BigDecimal> saidasPorCategoria;
    private List<TransactionResponse> recentTransactions;

    // Constructors
    public DashboardResponse() {}

    // Getters and Setters
    public BigDecimal getTotalEntradas() { return totalEntradas; }
    public void setTotalEntradas(BigDecimal totalEntradas) { this.totalEntradas = totalEntradas; }

    public BigDecimal getTotalSaidas() { return totalSaidas; }
    public void setTotalSaidas(BigDecimal totalSaidas) { this.totalSaidas = totalSaidas; }

    public BigDecimal getSaldo() { return saldo; }
    public void setSaldo(BigDecimal saldo) { this.saldo = saldo; }

    public Map<String, BigDecimal> getEntradasPorCategoria() { return entradasPorCategoria; }
    public void setEntradasPorCategoria(Map<String, BigDecimal> entradasPorCategoria) { this.entradasPorCategoria = entradasPorCategoria; }

    public Map<String, BigDecimal> getSaidasPorCategoria() { return saidasPorCategoria; }
    public void setSaidasPorCategoria(Map<String, BigDecimal> saidasPorCategoria) { this.saidasPorCategoria = saidasPorCategoria; }

    public List<TransactionResponse> getRecentTransactions() { return recentTransactions; }
    public void setRecentTransactions(List<TransactionResponse> recentTransactions) { this.recentTransactions = recentTransactions; }
}
