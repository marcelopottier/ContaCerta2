package com.univille.controle_financeiro.repository;
import com.univille.controle_financeiro.entity.Transaction;
import com.univille.controle_financeiro.entity.TransactionType;
import com.univille.controle_financeiro.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserOrderByDateDesc(User user);

    List<Transaction> findByUserAndDateBetweenOrderByDateDesc(User user, LocalDate startDate, LocalDate endDate);

    @Query("SELECT SUM(t.value) FROM Transaction t WHERE t.user = :user AND t.type = :type")
    BigDecimal sumByUserAndType(@Param("user") User user, @Param("type") TransactionType type);

    @Query("SELECT SUM(t.value) FROM Transaction t WHERE t.user = :user AND t.type = :type AND t.date BETWEEN :startDate AND :endDate")
    BigDecimal sumByUserAndTypeAndDateBetween(@Param("user") User user, @Param("type") TransactionType type,
                                              @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT t.category.name, SUM(t.value) FROM Transaction t WHERE t.user = :user AND t.type = :type GROUP BY t.category.name")
    List<Object[]> sumByUserAndTypeGroupByCategory(@Param("user") User user, @Param("type") TransactionType type);

    @Query("SELECT t.category.name, SUM(t.value) FROM Transaction t WHERE t.user = :user AND t.type = :type AND t.date BETWEEN :startDate AND :endDate GROUP BY t.category.name")
    List<Object[]> sumByUserAndTypeAndDateBetweenGroupByCategory(@Param("user") User user, @Param("type") TransactionType type,
                                                                 @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
