package com.polarbookshop.edgeservice.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.security.Principal;

@Configuration
public class RateLimiterConfig {

//    @Bean
//    //Ограничение скорости рассчитывается на основе общего количества запросов, получаемых каждую секунду
//    public KeyResolver keyResolver() {
//        return exchange -> Mono.just("anonymous");//Ограничение скорости применяется к запросам с использованием постоянного ключа.
//    }

    @Bean
    //Ограничение скорости запросов для конкретного клиента, получаемых каждую секунду
    KeyResolver keyResolver() {
        //Получает текущего аутентифицированного пользователя (принципал) из текущего запроса (обмена)
        return exchange -> exchange.getPrincipal()
                //Извлекает имя пользователя из принципала
                .map(Principal::getName)
                //Если запрос не аутентифицирован, он использует «анонимный» в качестве ключа по умолчанию для применения ограничения скорости
                .defaultIfEmpty("anonymous");
    }

}

