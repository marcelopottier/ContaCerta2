package com.univille.controle_financeiro.config;

import com.univille.controle_financeiro.entity.Category;
import com.univille.controle_financeiro.entity.Transaction;
import com.univille.controle_financeiro.entity.TransactionType;
import com.univille.controle_financeiro.entity.User;
import com.univille.controle_financeiro.repository.CategoryRepository;
import com.univille.controle_financeiro.repository.TransactionRepository;
import com.univille.controle_financeiro.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           CategoryRepository categoryRepository,
                           TransactionRepository transactionRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository        = userRepository;
        this.categoryRepository    = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.passwordEncoder       = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Usuário de teste já existente — pulando inicialização de dados.");
            return;
        }

        // Cria usuário de teste
        User user = new User();
        user.setName("Usuário Teste");
        user.setEmail("teste@email.com");
        user.setPasswordHash(passwordEncoder.encode("123456"));
        user = userRepository.save(user);

        // Cria categorias
        Category salario     = new Category("Salário", user);
        Category alimentacao = new Category("Alimentação", user);
        Category transporte  = new Category("Transporte", user);
        Category lazer       = new Category("Lazer", user);
        categoryRepository.saveAll(List.of(salario, alimentacao, transporte, lazer));

        // Cria transações
        Transaction t1 = new Transaction(
                TransactionType.ENTRADA,
                new BigDecimal("3000.00"),
                LocalDate.now().minusDays(5),
                "Salário mensal",
                user, salario
        );
        Transaction t2 = new Transaction(
                TransactionType.SAIDA,
                new BigDecimal("150.00"),
                LocalDate.now().minusDays(3),
                "Supermercado",
                user, alimentacao
        );
        Transaction t3 = new Transaction(
                TransactionType.SAIDA,
                new BigDecimal("80.00"),
                LocalDate.now().minusDays(2),
                "Combustível",
                user, transporte
        );
        Transaction t4 = new Transaction(
                TransactionType.SAIDA,
                new BigDecimal("120.00"),
                LocalDate.now().minusDays(1),
                "Cinema",
                user, lazer
        );
        transactionRepository.saveAll(List.of(t1, t2, t3, t4));

        log.info("Dados de teste criados com sucesso:");
        log.info("  • Usuário: {} / Senha: {}", user.getEmail(), "123456");
    }
}
