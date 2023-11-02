package org.example.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DirectExchangeConfiguration {
    private static final String EXCHANGE_NAME = "exchange.direct";
    private static final String QUEUE_A_NAME = "queue.direct.1";
    private static final String QUEUE_B_NAME = "queue.direct.2";
    public static final String ROUTING_KEY = "HELLO";


    @Bean
    public DirectExchange directExchange() {
        return ExchangeBuilder
                .directExchange(EXCHANGE_NAME)
                .build();
    }

    @Bean
    public Queue directQueue1() {
        return QueueBuilder
                .durable(QUEUE_A_NAME)
                .build();
    }

    @Bean
    public Queue directQueue2() {
        return QueueBuilder
                .durable(QUEUE_B_NAME)
                .build();
    }

    // Binding绑定方式太麻烦，准确地说是规范且繁琐。 => 设计模式 => 基于注解，动态生成（动态代理）
    @Bean
    public Binding directBinding1() {
        return BindingBuilder
                .bind(directQueue1()).to(directExchange())
                .with(ROUTING_KEY);
    }

    @Bean
    public Binding directBinding2() {
        return BindingBuilder
                .bind(directQueue2()).to(directExchange())
                .with(ROUTING_KEY);
    }
}
