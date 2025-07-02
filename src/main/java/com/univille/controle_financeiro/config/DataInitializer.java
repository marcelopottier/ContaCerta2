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
import java.time.LocalDateTime;
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
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Usuário de teste já existente — pulando inicialização de dados.");
            return;
        }

        try {
            log.info("🚀 Iniciando criação de dados de teste...");

            // Cria usuário de teste com dados completos
            User user = createTestUser();

            // Cria categorias mais abrangentes
            List<Category> categories = createCategories(user);

            // Cria transações de exemplo
            createSampleTransactions(user, categories);

            log.info("✅ Dados de teste criados com sucesso!");
            log.info("📧 Email: {}", user.getEmail());
            log.info("🔑 Senha: 123456");
            log.info("📊 {} categorias criadas", categories.size());
            log.info("💰 {} transações criadas", transactionRepository.count());

        } catch (Exception e) {
            log.error("❌ Erro ao criar dados de teste: {}", e.getMessage(), e);
        }
    }

    private User createTestUser() {
        User user = new User();
        user.setName("João Silva");
        user.setEmail("teste@email.com");
        user.setAvatarFileName("image.png");
        user.setPasswordHash(passwordEncoder.encode("123456"));

        // Adicionar campos adicionais se existirem na entidade
        try {
            user.setPhone("(11) 99999-9999");
            user.setDateOfBirth(LocalDate.of(1990, 5, 15));

            // Se os campos de timestamp existirem
            if (hasField(user, "createdAt")) {
                user.setCreatedAt(LocalDateTime.now().minusMonths(3));
            }
            if (hasField(user, "updatedAt")) {
                user.setUpdatedAt(LocalDateTime.now());
            }
        } catch (Exception e) {
            // Campos opcionais não existem, continuar normalmente
            log.debug("Alguns campos opcionais não estão disponíveis na entidade User");
        }

        return userRepository.save(user);
    }

    private List<Category> createCategories(User user) {
        List<Category> categories = List.of(
                // Receitas (tons de verde)
                new Category("Salário", user, "#4CAF50"),
                new Category("Freelance", user, "#8BC34A"),
                new Category("Investimentos", user, "#2E7D32"),

                // Gastos Essenciais (tons de azul)
                new Category("Alimentação", user, "#2196F3"),
                new Category("Moradia", user, "#1976D2"),
                new Category("Transporte", user, "#3F51B5"),
                new Category("Saúde", user, "#009688"),

                // Gastos Pessoais (tons de laranja/roxo)
                new Category("Lazer", user, "#FF9800"),
                new Category("Roupas", user, "#E91E63"),
                new Category("Tecnologia", user, "#9C27B0"),
                new Category("Educação", user, "#673AB7"),

                // Outros (tons de cinza/vermelho)
                new Category("Impostos", user, "#F44336"),
                new Category("Emergências", user, "#795548")
        );

        return categoryRepository.saveAll(categories);
    }

    private void createSampleTransactions(User user, List<Category> categories) {
        // Buscar categorias específicas
        Category salario = findCategory(categories, "Salário");
        Category alimentacao = findCategory(categories, "Alimentação");
        Category transporte = findCategory(categories, "Transporte");
        Category lazer = findCategory(categories, "Lazer");
        Category moradia = findCategory(categories, "Moradia");
        Category freelance = findCategory(categories, "Freelance");
        Category saude = findCategory(categories, "Saúde");
        Category tecnologia = findCategory(categories, "Tecnologia");

        // Transações do mês atual (para aparecer no dashboard)
        List<Transaction> currentMonthTransactions = List.of(
                // Receitas
                new Transaction(TransactionType.ENTRADA, new BigDecimal("3020.09"),
                        LocalDate.now().minusDays(5), "Salário", user, salario),

                new Transaction(TransactionType.ENTRADA, new BigDecimal("20.09"),
                        LocalDate.now(), "Pix recebido", user, salario),

                // Gastos recentes
                new Transaction(TransactionType.SAIDA, new BigDecimal("150.00"),
                        LocalDate.now().minusDays(3), "Supermercado", user, alimentacao),

                new Transaction(TransactionType.SAIDA, new BigDecimal("80.00"),
                        LocalDate.now().minusDays(2), "Combustível", user, transporte),

                new Transaction(TransactionType.SAIDA, new BigDecimal("120.00"),
                        LocalDate.now().minusDays(1), "Cinema", user, lazer)
        );

        // Transações do mês passado
        List<Transaction> lastMonthTransactions = List.of(
                new Transaction(TransactionType.ENTRADA, new BigDecimal("3000.00"),
                        LocalDate.now().minusMonths(1).withDayOfMonth(5), "Salário mês passado", user, salario),

                new Transaction(TransactionType.ENTRADA, new BigDecimal("800.00"),
                        LocalDate.now().minusMonths(1).withDayOfMonth(15), "Projeto freelance", user, freelance),

                new Transaction(TransactionType.SAIDA, new BigDecimal("1200.00"),
                        LocalDate.now().minusMonths(1).withDayOfMonth(10), "Aluguel", user, moradia),

                new Transaction(TransactionType.SAIDA, new BigDecimal("350.00"),
                        LocalDate.now().minusMonths(1).withDayOfMonth(12), "Contas de casa", user, moradia),

                new Transaction(TransactionType.SAIDA, new BigDecimal("200.00"),
                        LocalDate.now().minusMonths(1).withDayOfMonth(20), "Médico", user, saude),

                new Transaction(TransactionType.SAIDA, new BigDecimal("450.00"),
                        LocalDate.now().minusMonths(1).withDayOfMonth(25), "Compras alimentação", user, alimentacao)
        );

        // Algumas transações especiais
        List<Transaction> specialTransactions = List.of(
                new Transaction(TransactionType.SAIDA, new BigDecimal("2500.00"),
                        LocalDate.now().minusDays(60), "Notebook novo", user, tecnologia),

                new Transaction(TransactionType.ENTRADA, new BigDecimal("500.00"),
                        LocalDate.now().minusDays(45), "Rendimento investimentos", user,
                        findCategory(categories, "Investimentos"))
        );

        // Salvar todas as transações
        transactionRepository.saveAll(currentMonthTransactions);
        transactionRepository.saveAll(lastMonthTransactions);
        transactionRepository.saveAll(specialTransactions);

        log.info("📈 Transações criadas por período:");
        log.info("   • Mês atual: {} transações", currentMonthTransactions.size());
        log.info("   • Mês passado: {} transações", lastMonthTransactions.size());
        log.info("   • Especiais: {} transações", specialTransactions.size());
    }

    private Category findCategory(List<Category> categories, String name) {
        return categories.stream()
                .filter(c -> c.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada: " + name));
    }

    private boolean hasField(Object obj, String fieldName) {
        try {
            obj.getClass().getDeclaredField(fieldName);
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }
}