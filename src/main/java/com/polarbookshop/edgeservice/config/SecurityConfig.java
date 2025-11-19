package com.polarbookshop.edgeservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.server.WebSessionServerOAuth2AuthorizedClientRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public SecurityWebFilterChain springSecurityFilterChain(
            ServerHttpSecurity http,
            ReactiveClientRegistrationRepository clientRegistrationRepository
    ) {
        return http
                .authorizeExchange(exchange -> exchange
                        //Разрешает не аутентифицированный доступ к статическим ресурсам SPA.
                        .pathMatchers("/", "/*.css", "/*.js", "/favicon.ico").permitAll()
                        //Разрешает не аутентифицированный доступ для чтения к книгам в каталоге.
                        .pathMatchers(HttpMethod.GET, "/books/**").permitAll()
                        .anyExchange().authenticated()//Все остальные запросы требуют аутентификации.
                )
                //Когда исключение создается из-за того, что пользователь не прошел проверку подлинности, он отвечает ответом HTTP 401
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling.authenticationEntryPoint(
                                new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)))
                //.formLogin(Customizer.withDefaults())//Включает аутентификацию пользователя через форму входа.
                .oauth2Login(Customizer.withDefaults())//Включает аутентификацию пользователя с помощью OAuth2/OpenID Connect.
                .logout(logout -> logout.logoutSuccessHandler(//Определяет пользовательский обработчик для сценария, в котором операция выхода из системы завершается успешно.
                        oidcLogoutSuccessHandler(clientRegistrationRepository)))
                //Использует стратегию на основе файлов cookie для обмена токенами CSRF с интерфейсом Angular.
                //.csrf(csrf -> csrf.csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse()))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .build();
    }

    private ServerLogoutSuccessHandler oidcLogoutSuccessHandler(
            ReactiveClientRegistrationRepository clientRegistrationRepository
    ) {
        var oidcLogoutSuccessHandler = new OidcClientInitiatedServerLogoutSuccessHandler(clientRegistrationRepository);
        //После выхода из провайдера OIDC Keycloak перенаправит пользователя на базовый URL-адрес приложения, динамически вычисляемый из Spring
        //(локально это http://localhost:9000)
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}");
        return oidcLogoutSuccessHandler;
    }

    //Определяет репозиторий для хранения токенов доступа в веб-сеансе.
//    @Bean
//    public ServerOAuth2AuthorizedClientRepository authorizedClientRepository() {
//        return new WebSessionServerOAuth2AuthorizedClientRepository();
//    }

    //Фильтр, единственной целью которого является подписка на реактивный поток CsrfToken и обеспечение правильного извлечения его значения.
//    @Bean
//    public WebFilter csrfWebFilter() {
//        return (exchange, chain) -> {
//            exchange.getResponse().beforeCommit(() -> Mono.defer(() -> {
//                Mono<CsrfToken> csrfToken =
//                        exchange.getAttribute(CsrfToken.class.getName());
//                return csrfToken != null ? csrfToken.then() : Mono.empty();
//            }));
//            return chain.filter(exchange);
//        };
//    }

//    @Bean
//    WebFilter csrfCookieWebFilter() {
//        return (exchange, chain) -> {
//            // Этот фильтр гарантирует, что CSRF токен будет установлен в cookie
//            Mono<CsrfToken> csrfToken = exchange.getAttribute(CsrfToken.class.getName());
//            if (csrfToken != null) {
//                return csrfToken.then(chain.filter(exchange));
//            }
//            return chain.filter(exchange);
//        };
//    }



//    @Bean
//    WebFilter debugCsrfFilter() {
//        return (exchange, chain) -> {
//            System.out.println("=== DEBUG CSRF ===");
//            System.out.println("Request: " + exchange.getRequest().getMethod() + " " + exchange.getRequest().getPath());
//
//            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
//                Mono<CsrfToken> csrfTokenMono = exchange.getAttribute(CsrfToken.class.getName());
//                if (csrfTokenMono != null) {
//                    csrfTokenMono.subscribe(token -> {
//                        System.out.println("CSRF Token generated: " + token.getToken());
//                        System.out.println("CSRF Token name: " + token.getHeaderName());
//                    });
//                } else {
//                    System.out.println("No CSRF token generated");
//                }
//            }));
//        };
//    }

//    @Bean
//    public ApplicationRunner csrfDebugRunner() {
//        return args -> {
//            System.out.println("=== CSRF CONFIGURATION DEBUG ===");
//            System.out.println("CSRF should be DISABLED in SecurityConfig");
//        };
//    }
}
