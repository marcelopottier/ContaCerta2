package com.univille.controle_financeiro.repository;

import com.univille.controle_financeiro.entity.Category;
import com.univille.controle_financeiro.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUser(User user);
    boolean existsByNameAndUser(String name, User user);
    Optional<Category> findByIdAndUser(Long id, User user);
    List<Category> findByUserOrderByNameAsc(User user);
}