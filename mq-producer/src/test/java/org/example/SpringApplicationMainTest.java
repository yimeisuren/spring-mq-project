package org.example;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.util.concurrent.SettableListenableFuture;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@SpringBootTest
public class SpringApplicationMainTest {
    private static final String ROUTING_KEY = "HELLO";
    private static final String ERROR_ROUTING_KEY = "ERROR";
    private static final String ERROR_EXCHANGE = "ERROR_EXCHANGE";
    private static final String EXCHANGE = "exchange.direct";
    private static final String pageOutExchange = "simple.exchange";

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void sendStringMsgTest() {
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, "hello world");
    }

    @Test
    public void sendObjectMsgTest() {
        Map<String, String> map = new HashMap<>();
        map.put("username", "root");
        map.put("password", "root");

        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, map);
    }

    @Test
    public void errorReturnCallbackTest() {
        rabbitTemplate.convertAndSend(EXCHANGE, ERROR_ROUTING_KEY, "hello world");
    }

    @Test
    public void confirmCallbackTest() {
        CorrelationData correlationData = new CorrelationData();
        SettableListenableFuture<CorrelationData.Confirm> future = correlationData.getFuture();
        future.addCallback(new ListenableFutureCallback<CorrelationData.Confirm>() {
            @Override
            public void onFailure(Throwable ex) {
                // TODO: 什么情况下会进入到这里呢?
                //  这里的失败表示Spring内部处理失败, 和MQ无关, 几乎不会进入到这里
                log.error("Spring 内部出现异常");
            }

            @Override
            public void onSuccess(CorrelationData.Confirm result) {
                if (result.isAck()) {
                    log.info("正常发送消息, ACK正常");
                } else {
                    String reason = result.getReason();
                    log.error("NACK, reason: {}", reason);
                }
            }
        });

        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, "hello world", correlationData);
    }


    // 传递错误的路由路径
    @Test
    public void confirmCallbackAndReturnCallback1Test() {
        CorrelationData correlationData = new CorrelationData();
        SettableListenableFuture<CorrelationData.Confirm> future = correlationData.getFuture();
        future.addCallback(new ListenableFutureCallback<CorrelationData.Confirm>() {
            @Override
            public void onFailure(Throwable ex) {
                // 什么情况下会进入到这里呢?
                //  这里的失败表示Spring内部处理失败, 和MQ无关, 几乎不会进入到这里
                log.error("Spring 内部出现异常");
            }

            @Override
            public void onSuccess(CorrelationData.Confirm result) {
                if (result.isAck()) {
                    log.info("正常发送消息, ACK正常");
                } else {
                    String reason = result.getReason();
                    log.error("NACK, reason: {}", reason);
                }
            }
        });

        // 这里传递一个错误的路由路径, confirmCallback正常返回ACK, 因为exchange接收是正常的
        rabbitTemplate.convertAndSend(EXCHANGE, ERROR_ROUTING_KEY, "hello world", correlationData);
    }

    // 传递错误的路由路径
    @Test
    public void confirmCallbackAndReturnCallback2Test() {
        CorrelationData correlationData = new CorrelationData();
        SettableListenableFuture<CorrelationData.Confirm> future = correlationData.getFuture();
        future.addCallback(new ListenableFutureCallback<CorrelationData.Confirm>() {
            @Override
            public void onFailure(Throwable ex) {
                log.error("Spring 内部出现异常");
            }

            @Override
            public void onSuccess(CorrelationData.Confirm result) {
                if (result.isAck()) {
                    log.info("正常发送消息, ACK正常");
                } else {
                    // NACK: 可能因为内存/磁盘等, 或MQ内部出现问题造成消息发送失败. 真正可能出现的故障就是NACK, 其他人为原因(配置错误、代码错误)不考虑在内, 因此在这里进行消息重发即可.
                    String reason = result.getReason();
                    log.error("NACK, reason: {}", reason);
                }
            }
        });

        rabbitTemplate.convertAndSend(ERROR_EXCHANGE, ROUTING_KEY, "hello world", correlationData);
    }


    // 在演示pageOut问题时，关闭Publisher-Confirm机制，发送速度达到每秒30-40k左右才算正常
    // 建议使用绑定单个queue的exchange，这样可以看到在pageOut过程中，发送消息速率会下降到0，即MQ被阻塞
    // 可能也不会下降到0，新版本的MQ使用LazyQueue（惰性队列）来进行优化，并且默认都是惰性队列，不能更改队列类型
    @Test
    public void pageOutTest() {
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setDeliveryMode(MessageDeliveryMode.NON_PERSISTENT);
        Message message = new Message("hello world".getBytes(StandardCharsets.UTF_8), messageProperties);

        for (int i = 0; i < 1000_0000; i++) {
            rabbitTemplate.convertAndSend(pageOutExchange, message);
        }
    }

    // 延时消息
    @Test
    public void ttlMessageTest() {
        // 延时消息的设置
        // !!! 使用Message对象发送消息会因为使用的是Jackson2JsonMessageConverter这个消息转换器而产生异常??? 解决办法是使用convertAndSend中含有消息后置处理器的重载方法 (有待验证)
        // Message message = MessageBuilder.withBody("hello world".getBytes(StandardCharsets.UTF_8))
        //         .setExpiration("10_000")
        //         .build();

        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, "hello world", new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                message.getMessageProperties().setExpiration("10000");
                return message;
            }
        });

    }
}
