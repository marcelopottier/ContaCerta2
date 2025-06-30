package com.univille.controle_financeiro.repository;

import com.univille.controle_financeiro.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca usuário por email (case insensitive)
     */
    Optional<User> findByEmail(String email);

    /**
     * Verifica se existe usuário com o email
     */
    boolean existsByEmail(String email);

    /**
     * Busca usuário por email ignorando case
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    Optional<User> findByEmailIgnoreCase(@Param("email") String email);

    /**
     * Verifica se existe usuário com email ignorando case
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    boolean existsByEmailIgnoreCase(@Param("email") String email);

    /**
     * Busca usuários por nome (busca parcial)
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<User> findByNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Conta usuários criados entre duas datas
     */
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Busca usuários criados após uma data
     */
    List<User> findByCreatedAtAfter(LocalDateTime date);

    /**
     * Busca usuários ordenados por data de criação (mais recentes primeiro)
     */
    List<User> findAllByOrderByCreatedAtDesc();

    /**
     * Busca usuários com telefone preenchido
     */
    @Query("SELECT u FROM User u WHERE u.phone IS NOT NULL AND u.phone != ''")
    List<User> findUsersWithPhone();

    /**
     * Busca usuários com data de nascimento preenchida
     */
    @Query("SELECT u FROM User u WHERE u.dateOfBirth IS NOT NULL")
    List<User> findUsersWithDateOfBirth();

    /**
     * Busca usuários com perfil completo
     */
    @Query("SELECT u FROM User u WHERE u.name IS NOT NULL AND u.email IS NOT NULL " +
            "AND u.phone IS NOT NULL AND u.phone != '' " +
            "AND u.dateOfBirth IS NOT NULL")
    List<User> findUsersWithCompleteProfile();

    /**
     * Conta usuários com perfil completo
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.name IS NOT NULL AND u.email IS NOT NULL " +
            "AND u.phone IS NOT NULL AND u.phone != '' " +
            "AND u.dateOfBirth IS NOT NULL")
    long countUsersWithCompleteProfile();

    /**
     * Busca usuários ativos (que fizeram login recentemente)
     */
    @Query("SELECT u FROM User u WHERE u.updatedAt >= :since")
    List<User> findActiveUsersSince(@Param("since") LocalDateTime since);

    /**
     * Conta usuários ativos
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.updatedAt >= :since")
    long countActiveUsersSince(@Param("since") LocalDateTime since);

    /**
     * Busca usuários por domínio do email
     */
    @Query("SELECT u FROM User u WHERE u.email LIKE CONCAT('%@', :domain)")
    List<User> findByEmailDomain(@Param("domain") String domain);

    /**
     * Estatísticas gerais dos usuários
     */
    @Query("SELECT " +
            "COUNT(u) as total, " +
            "COUNT(CASE WHEN u.phone IS NOT NULL AND u.phone != '' THEN 1 END) as withPhone, " +
            "COUNT(CASE WHEN u.dateOfBirth IS NOT NULL THEN 1 END) as withBirthDate " +
            "FROM User u")
    UserStatistics getUserStatistics();

    /**
     * Interface para projeção de estatísticas
     */
    interface UserStatistics {
        long getTotal();
        long getWithPhone();
        long getWithBirthDate();
    }

    /**
     * Busca usuários paginados ordenados por nome
     */
    @Query("SELECT u FROM User u ORDER BY u.name ASC")
    List<User> findAllOrderByName();

    /**
     * Verifica se existe usuário com email diferente do ID fornecido
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE LOWER(u.email) = LOWER(:email) AND u.id != :id")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("id") Long id);

    /**
     * Deleta usuários inativos há mais de X tempo
     */
    @Query("DELETE FROM User u WHERE u.updatedAt < :cutoffDate")
    void deleteInactiveUsersBefore(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Busca último usuário criado
     */
    @Query("SELECT u FROM User u ORDER BY u.createdAt DESC")
    Optional<User> findLatestUser();

    /**
     * Conta usuários por mês de criação (últimos 12 meses)
     */
    @Query("SELECT " +
            "YEAR(u.createdAt) as year, " +
            "MONTH(u.createdAt) as month, " +
            "COUNT(u) as count " +
            "FROM User u " +
            "WHERE u.createdAt >= :since " +
            "GROUP BY YEAR(u.createdAt), MONTH(u.createdAt) " +
            "ORDER BY year DESC, month DESC")
    List<MonthlyUserCount> countUsersByMonth(@Param("since") LocalDateTime since);

    /**
     * Interface para contagem mensal
     */
    interface MonthlyUserCount {
        int getYear();
        int getMonth();
        long getCount();
    }
}