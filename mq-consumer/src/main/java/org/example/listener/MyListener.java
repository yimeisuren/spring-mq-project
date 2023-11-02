package org.example.listener;

import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class MyListener {
    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue(name = "direct.queue.annotation.1", durable = "true"),
                    exchange = @Exchange(name = "direct.exchange.annotation.1", type = ExchangeTypes.DIRECT),
                    key = {"direct.annotation.1", "direct.annotation.2"}
            ),
            @QueueBinding(
                    value = @Queue(name = "direct.queue.annotation.2", durable = "true"),
                    exchange = @Exchange(name = "direct.exchange.annotation.2", type = ExchangeTypes.TOPIC),
                    key = {"direct.annotation.3", "direct.annotation.4"}
            )
    })
    public void show(String msg) {
        System.out.println("msg = " + msg);
    }

    @RabbitListener(queues = "queue.direct.1")
    public void hello(String msg) {
        System.out.println(msg);
    }


}
