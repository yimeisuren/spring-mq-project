package org.example.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageConverterConfiguration {
    // 如果没有使用该消息转换器，在消费端会产生无法将Map对象转换为String。
    // TODO: 猜测这些消息转换器的原理是将对象中的属性和值转换成key-value的字符串表示，然后再对字符串传输
    @Bean
    public MessageConverter messageConverter() {
        Jackson2JsonMessageConverter jackson2JsonMessageConverter = new Jackson2JsonMessageConverter();
        // 这里生成的id可以用于处理消息的幂等性要求，生成方法是UUID.randomUUID()，可以在convertAndSend方法中进行跟踪
        // TODO: 如果不想要使用默认的生成策略，也可以配置一个消息后置处理器，为生成的消息手动配置MessageProperties中的属性即可。message_id属性
        jackson2JsonMessageConverter.setCreateMessageIds(true);
        return jackson2JsonMessageConverter;
    }
}
