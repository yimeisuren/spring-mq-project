package org.example.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FanoutExchangeConfiguration {

    @Bean
    public FanoutExchange fanoutExchange() {
        return ExchangeBuilder
                .fanoutExchange("exchange.fanout")
                .build();
    }

    @Bean
    public Queue fanoutQueue1() {
        return QueueBuilder
                .durable("queue.fanout.1")
                .build();
    }

    @Bean
    public Queue fanoutQueue2() {
        return QueueBuilder
                .durable("queue.fanout.2")
                .build();
    }

    @Bean
    public Binding fanoutBinding1() {
        // Fanout Exchange 不需要指定bindingKey
        // 这里的调用queue1()实际上不会去真正调用当前类的方法，而是去Spring容器中获取动态代理后的对象，因此不用担心得到的是一个原生对象。
        return BindingBuilder
                .bind(fanoutQueue1()).to(fanoutExchange());
    }

    @Bean
    public Binding fanoutBinding2() {
        return BindingBuilder
                .bind(fanoutQueue2()).to(fanoutExchange());
    }

}
