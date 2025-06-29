package com.univille.controle_financeiro.service;

import com.univille.controle_financeiro.dto.TransactionResponse;
import com.univille.controle_financeiro.entity.Category;
import com.univille.controle_financeiro.entity.Transaction;
import com.univille.controle_financeiro.entity.TransactionType;
import com.univille.controle_financeiro.entity.User;
import com.univille.controle_financeiro.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.awt.print.Pageable;
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
    /*
    public Page<TransactionResponse> findTransactionsWithFilters(User user,
                                                                 LocalDate startDate,
                                                                 LocalDate endDate,
                                                                 Long categoryId,
                                                                 String type,
                                                                 String search,
                                                                 Pageable pageable) {

        // Criar especificação dinâmica para filtros
        Specification<Transaction> spec = Specification.where(null);

        // Filtro por usuário (sempre obrigatório)
        spec = spec.and((root, query, cb) -> cb.equal(root.get("user"), user));

        // Filtro por período
        if (startDate != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("date"), startDate));
        }
        if (endDate != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("date"), endDate));
        }

        // Filtro por categoria
        if (categoryId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId));
        }

        // Filtro por tipo
        if (type != null && !type.isEmpty()) {
            TransactionType transactionType = TransactionType.valueOf(type.toUpperCase());
            spec = spec.and((root, query, cb) -> cb.equal(root.get("type"), transactionType));
        }

        // Filtro por descrição
        if (search != null && !search.trim().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("description")), "%" + search.toLowerCase() + "%"));
        }

        // Aplicar ordenação
        Sort sort = pageable.getSort();
        if (sort.isUnsorted()) {
            sort = Sort.by(Sort.Direction.DESC, "date");
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        }

        Page<Transaction> transactions = transactionRepository.findAll(spec, pageable);

        return transactions.map(this::convertToResponse);
    }*/
}