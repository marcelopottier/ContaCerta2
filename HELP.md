# FinanceControl - Refatoração Front-end

## 📋 Visão Geral

Esta refatoração transforma o sistema de controle financeiro em uma aplicação moderna, responsiva e acessível usando **Thymeleaf** + **Bootstrap 5**.

### ✨ Principais Melhorias

- **🎨 UI/UX Moderna**: Interface limpa com design system consistente
- **📱 Mobile-First**: Totalmente responsivo para todos os dispositivos
- **♿ Acessibilidade**: Seguindo padrões WCAG com navegação por teclado
- **🧩 Componentização**: Fragments Thymeleaf reutilizáveis
- **⚡ Performance**: CSS otimizado e JavaScript modular
- **🔒 Segurança**: Integração completa com Spring Security

## 🏗️ Arquitetura

### Estrutura de Arquivos

```
src/main/
├── java/com/univille/controle_financeiro/
│   └── controller/
│       └── WebController.java                 # Controller das páginas web
├── resources/
│   ├── static/
│   │   ├── css/
│   │   │   └── main.css                      # Estilos principais
│   │   └── js/
│   │       └── main.js                       # JavaScript utilitário
│   └── templates/
│       ├── layout.html                       # Layout base
│       ├── index.html                        # Página inicial
│       ├── login.html                        # Login
│       ├── register.html                     # Cadastro
│       ├── dashboard.html                    # Dashboard principal
│       ├── categories.html                   # CRUD Categorias
│       ├── transactions.html                 # CRUD Transações
│       ├── profile.html                      # Perfil do usuário
│       └── fragments/
│           ├── navbar.html                   # Navegação
│           ├── components.html               # Componentes reutilizáveis
│           └── transaction-modal.html        # Modal de transações
```

### Design System

- **Paleta de Cores**: Tons neutros com roxo como cor primária
- **Tipografia**: Segoe UI para máxima legibilidade
- **Espaçamentos**: Sistema consistente baseado em múltiplos de 4px
- **Animações**: Transições suaves de 0.3s

## 🚀 Como Usar

### 1. Integração com Backend Existente

A refatoração mantém **100% de compatibilidade** com o backend atual. Apenas adicione:

#### WebController.java
```java
// Copie o arquivo WebController.java para:
// src/main/java/com/univille/controle_financeiro/controller/WebController.java
```

#### Métodos Adicionais nos Services
```java
// Adicione os métodos extras no TransactionService e UserService
// conforme especificado nos arquivos de extensão
```

### 2. Configuração de Recursos Estáticos

```java
// Em WebConfig.java (criar se não existir)
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");
    }
}
```

### 3. Configuração do Thymeleaf Layout

```properties
# Em application.properties
spring.thymeleaf.mode=HTML
spring.thymeleaf.encoding=UTF-8
spring.thymeleaf.cache=false
```

### 4. Dependências Maven

```xml
<!-- Adicionar se não existir -->
<dependency>
    <groupId>nz.net.ultraq.thymeleaf</groupId>
    <artifactId>thymeleaf-layout-dialect</artifactId>
</dependency>
```

## 📱 Funcionalidades

### Dashboard
- **📊 Gráficos Interativos**: Chart.js para visualização de dados
- **💰 Cards de Estatísticas**: Entradas, saídas e saldo
- **📋 Transações Recentes**: Lista das últimas movimentações
- **➕ Ação Rápida**: Botão flutuante para nova transação

### Gestão de Categorias
- **🏷️ CRUD Completo**: Criar, editar e excluir categorias
- **🎨 Seletor de Cores**: Interface visual para escolha de cores
- **🔍 Busca**: Filtro em tempo real
- **📈 Estatísticas**: Uso por categoria

### Controle de Transações
- **📊 Filtros Avançados**: Por data, categoria, tipo e descrição
- **📄 Paginação**: Navegação eficiente em grandes volumes
- **💾 Exportação**: Excel, CSV e PDF
- **📱 Layout Responsivo**: Cards no mobile, tabela no desktop

### Perfil do Usuário
- **👤 Informações Pessoais**: Edição de dados
- **🔐 Alteração de Senha**: Com indicador de força
- **⚙️ Configurações**: Notificações e preferências
- **📊 Estatísticas**: Uso da plataforma

## 🎨 Customização

### Cores
```css
:root {
    --primary-color: #6f42c1;        /* Roxo principal */
    --primary-dark: #5a2d91;         /* Roxo escuro */
    --primary-light: #8e6dc7;        /* Roxo claro */
    --success-color: #198754;        /* Verde */
    --danger-color: #dc3545;         /* Vermelho */
    --warning-color: #ffc107;        /* Amarelo */
}
```

### Responsividade
- **Mobile**: < 768px (design prioritário)
- **Tablet**: 768px - 1024px
- **Desktop**: > 1024px

### Componentes Reutilizáveis

#### Alerts
```html
<div th:replace="~{fragments/components :: alerts}"></div>
```

#### Modais
```html
<div th:replace="~{fragments/transaction-modal :: transaction-modal}"></div>
```

#### Loading
```html
<div th:replace="~{fragments/components :: loading-spinner}"></div>
```

## 🔧 JavaScript Utilitário

### FinanceApp Global
```javascript
// Autenticação
FinanceApp.auth.isAuthenticated()
FinanceApp.auth.getCurrentUser()
FinanceApp.auth.logout()

// HTTP Requests
FinanceApp.http.get('/api/endpoint')
FinanceApp.http.post('/api/endpoint', data)

// UI Helpers
FinanceApp.ui.showAlert('Mensagem', 'success')
FinanceApp.ui.showLoading(true)

// Formatação
FinanceApp.format.currency(1234.56)  // R$ 1.234,56
FinanceApp.format.date('2024-01-15') // 15/01/2024
```

## 📋 Checklist de Implementação

### ✅ Estrutura Base
- [x] Layout principal com navegação
- [x] Sistema de componentes
- [x] CSS responsivo e moderno
- [x] JavaScript utilitário

### ✅ Páginas Principais
- [x] Página inicial (index.html)
- [x] Login e registro
- [x] Dashboard com gráficos
- [x] CRUD de categorias
- [x] CRUD de transações
- [x] Perfil do usuário

### ✅ Funcionalidades
- [x] Autenticação segura
- [x] Filtros e busca
- [x] Paginação
- [x] Modais interativos
- [x] Validação de formulários
- [x] Feedback visual

### 🔄 Próximos Passos
- [ ] Implementar exportação de dados
- [ ] Adicionar modo escuro
- [ ] PWA (Progressive Web App)
- [ ] Notificações push
- [ ] Relatórios avançados

## 🐛 Solução de Problemas

### CSS não carregando
```java
// Verificar configuração de recursos estáticos em WebConfig
// Confirmar paths em application.properties
```

### JavaScript com erro
```javascript
// Verificar se FinanceApp está sendo carregado
console.log(window.FinanceApp);
```

### Layout quebrado
```html
<!-- Verificar se layout.html está sendo usado corretamente -->
<html layout:decorate="~{layout}">
```

### Formulários não funcionando
```html
<!-- Verificar CSRF token -->
<input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>
```

## 📞 Suporte

Para dúvidas sobre a implementação:

1. **Documentação**: Verificar comentários nos arquivos
2. **Console**: Usar F12 para debuggar JavaScript
3. **Logs**: Verificar logs do Spring Boot
4. **Thymeleaf**: Consultar [documentação oficial](https://www.thymeleaf.org/)

## 🏆 Resultado Final

Uma aplicação moderna, profissional e user-friendly que oferece:

- **Experiência de usuário superior** com interface intuitiva
- **Performance otimizada** para todos os dispositivos
- **Código maintível** com componentes reutilizáveis
- **Acessibilidade completa** seguindo padrões web
- **Integração perfeita** com o backend Spring Boot existente

---

**Desenvolvido por**: Ian Sergio, Marcelo Pottier e Pedro Hideki  
**Tecnologias**: Spring Boot + Thymeleaf + Bootstrap 5 + Chart.js