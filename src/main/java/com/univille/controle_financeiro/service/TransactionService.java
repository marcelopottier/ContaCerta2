package com.univille.controle_financeiro.service;

import com.univille.controle_financeiro.dto.TransactionResponse;
import com.univille.controle_financeiro.entity.Category;
import com.univille.controle_financeiro.entity.Transaction;
import com.univille.controle_financeiro.entity.TransactionType;
import com.univille.controle_financeiro.entity.User;
import com.univille.controle_financeiro.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    public List<TransactionResponse> findByUser(User user) {
        return transactionRepository.findByUserOrderByDateDesc(user)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<TransactionResponse> findByUserAndPeriod(User user, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findByUserAndDateBetweenOrderByDateDesc(user, startDate, endDate)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public Transaction createTransaction(TransactionType type, BigDecimal value, LocalDate date,
                                         String description, User user, Category category) {
        Transaction transaction = new Transaction();
        transaction.setType(type);
        transaction.setValue(value);
        transaction.setDate(date);
        transaction.setDescription(description);
        transaction.setUser(user);
        transaction.setCategory(category);

        return transactionRepository.save(transaction);
    }

    public Transaction updateTransaction(Long id, TransactionType type, BigDecimal value, LocalDate date,
                                         String description, User user, Category category) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transação não encontrada"));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Acesso negado");
        }

        transaction.setType(type);
        transaction.setValue(value);
        transaction.setDate(date);
        transaction.setDescription(description);
        transaction.setCategory(category);

        return transactionRepository.save(transaction);
    }

    public void deleteTransaction(Long id, User user) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transação não encontrada"));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Acesso negado");
        }

        transactionRepository.delete(transaction);
    }

    public BigDecimal getTotalByType(User user, TransactionType type) {
        BigDecimal total = transactionRepository.sumByUserAndType(user, type);
        return total != null ? total : BigDecimal.ZERO;
    }

    public BigDecimal getTotalByTypeAndPeriod(User user, TransactionType type, LocalDate startDate, LocalDate endDate) {
        BigDecimal total = transactionRepository.sumByUserAndTypeAndDateBetween(user, type, startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    public Map<String, BigDecimal> getTotalByTypeGroupByCategory(User user, TransactionType type) {
        List<Object[]> results = transactionRepository.sumByUserAndTypeGroupByCategory(user, type);
        Map<String, BigDecimal> map = new HashMap<>();

        for (Object[] result : results) {
            String categoryName = (String) result[0];
            BigDecimal total = (BigDecimal) result[1];
            map.put(categoryName, total);
        }

        return map;
    }

    public Map<String, BigDecimal> getTotalByTypeAndPeriodGroupByCategory(User user, TransactionType type,
                                                                          LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = transactionRepository.sumByUserAndTypeAndDateBetweenGroupByCategory(user, type, startDate, endDate);
        Map<String, BigDecimal> map = new HashMap<>();

        for (Object[] result : results) {
            String categoryName = (String) result[0];
            BigDecimal total = (BigDecimal) result[1];
            map.put(categoryName, total);
        }

        return map;
    }

    private TransactionResponse convertToResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getType(),
                transaction.getValue(),
                transaction.getDate(),
                transaction.getDescription(),
                transaction.getCategory().getName()
        );
    }
}