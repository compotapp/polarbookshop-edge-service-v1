package com.polarbookshop.edgeservice.web;

import com.polarbookshop.edgeservice.user.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
public class UserController {

    //Получить данные из контекста
    @GetMapping("user/1")
    public Mono<User> getUser() {
        return ReactiveSecurityContextHolder.getContext()//Получает SecurityContext для текущего аутентифицированного пользователя из ReactiveSecurityContextHolder.
                .map(SecurityContext::getAuthentication)//Получает аутентификацию от SecurityContext
                .map(authentication ->//Получает субъект из проверки подлинности. Для OIDC это тип OidcUser.
                        (OidcUser) authentication.getPrincipal())
                .map(oidcUser ->//Создает объект User, используя данные из OidcUser (извлеченные из токена идентификатора).
                        new User(
                                oidcUser.getPreferredUsername(),
                                oidcUser.getGivenName(),
                                oidcUser.getFamilyName(),
                                List.of("employee", "customer")
                        )
                );
    }

    //Получить данные через аннотацию, аналог примера выше
    @GetMapping("user")
    public Mono<User> getUser(
            //Внедряет объект OidcUser, содержащий информацию о текущем аутентифицированном пользователе.
            @AuthenticationPrincipal OidcUser oidcUser
    ) {
        var user = new User(
                oidcUser.getPreferredUsername(),
                oidcUser.getGivenName(),
                oidcUser.getFamilyName(),
                oidcUser.getClaimAsStringList("roles")
        );
        return Mono.just(user);
    }
}
