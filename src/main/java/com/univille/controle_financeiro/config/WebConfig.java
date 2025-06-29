package com.univille.controle_financeiro.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Arrays;
import java.util.Locale;

/**
 * Configuração MVC para recursos estáticos, internacionalização e views
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configuração de recursos estáticos
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        boolean isProduction = false;
        int cacheSeconds = isProduction ? 31556926 : 0;

        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/")
                .setCachePeriod(3600)
                .resourceChain(true);

        // JavaScript
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/")
                .setCachePeriod(3600)
                .resourceChain(true);

        // Imagens
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/")
                .setCachePeriod(31556926)
                .resourceChain(true);

        // Fonts
        registry.addResourceHandler("/fonts/**")
                .addResourceLocations("classpath:/static/fonts/")
                .setCachePeriod(31556926)
                .resourceChain(true);

        // Favicon
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/favicon.ico")
                .setCachePeriod(31556926);
    }

    /**
     * Configuração de controllers simples para páginas estáticas
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Página de erro personalizada
        registry.addViewController("/error").setViewName("error");

        // Redirecionamento para HTTPS em produção (opcional)
        // registry.addRedirectViewController("/", "https://seudominio.com/");
    }

    /**
     * Resolver de locale para internacionalização
     */
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(new Locale("pt", "BR"));
        return slr;
    }

    /**
     * Interceptor para mudança de idioma
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang");
        return lci;
    }

    /**
     * Registrar interceptors
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());

        // Interceptor personalizado para logging ou auditoria (opcional)
        registry.addInterceptor(new RequestLoggingInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/css/**", "/js/**", "/images/**", "/fonts/**");
    }

    /**
     * Interceptor customizado para log de requests (opcional)
     */
    public static class RequestLoggingInterceptor implements org.springframework.web.servlet.HandlerInterceptor {

        private static final org.slf4j.Logger logger =
                org.slf4j.LoggerFactory.getLogger(RequestLoggingInterceptor.class);

        @Override
        public boolean preHandle(jakarta.servlet.http.HttpServletRequest request,
                                 jakarta.servlet.http.HttpServletResponse response,
                                 Object handler) throws Exception {

            // Log apenas em modo DEBUG para não poluir logs de produção
            if (logger.isDebugEnabled()) {
                String method = request.getMethod();
                String uri = request.getRequestURI();
                String userAgent = request.getHeader("User-Agent");

                logger.debug("Request: {} {} - User-Agent: {}", method, uri, userAgent);
            }

            return true;
        }
    }
}

/**
 * Configuração adicional para Thymeleaf Layout Dialect
 */
@Configuration
class ThymeleafConfig {

    @Bean
    public nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect layoutDialect() {
        return new nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect();
    }
}

/**
 * Configuração de compressão GZIP para recursos estáticos
 */
@Configuration
class CompressionConfig {

    @Bean
    public org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory servletContainer() {
        org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory tomcat =
                new org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory();

        tomcat.addConnectorCustomizers(connector -> {
            connector.setProperty("compression", "on");
            connector.setProperty("compressableMimeType",
                    "text/html,text/xml,text/plain,text/css,text/javascript,application/javascript,application/json");
            connector.setProperty("compressionMinSize", "1024");
        });

        return tomcat;
    }
}

@Configuration
class StaticResourceSecurityConfig {

    /**
     * Headers de segurança para recursos estáticos
     */
    @Bean
    public org.springframework.web.filter.OncePerRequestFilter securityHeadersFilter() {
        return new org.springframework.web.filter.OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(
                    jakarta.servlet.http.HttpServletRequest request,
                    jakarta.servlet.http.HttpServletResponse response,
                    jakarta.servlet.FilterChain filterChain
            ) throws jakarta.servlet.ServletException, java.io.IOException {

                String path = request.getRequestURI();

                // Aplicar headers de segurança apenas para recursos estáticos
                if (path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/images/")) {
                    response.setHeader("X-Content-Type-Options", "nosniff");
                    response.setHeader("X-Frame-Options", "DENY");
                    response.setHeader("X-XSS-Protection", "1; mode=block");
                }

                filterChain.doFilter(request, response);
            }
        };
    }
}