package org.example.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Slf4j
@Configuration
public class ReturnCallbackConfiguration implements ApplicationContextAware {

    @Autowired
    private RabbitTemplate rabbitTemplateA;

    {
        // 直接使用这种方式的话，根据Spring的生命周期，rabbitTemplateA对象此时还是null，在构造方法完成后才会通过属性注入为RabbitTemplate属性进行赋值注入
        // 因此直接在代码块中对rabbitTemplate进行设置会报错
        log.error("rabbitTemplateA = {}", rabbitTemplateA);
    }

    // 使用@PostConstruct注解保证在对象构造完成后调用，而@Autowired在构造器方法之后的属性注入阶段赋值
    @PostConstruct
    public void configRabbitTemplate() {
        rabbitTemplateA.setReturnsCallback(new RabbitTemplate.ReturnsCallback() {
            @Override
            public void returnedMessage(ReturnedMessage returned) {
                int replyCode = returned.getReplyCode();
                String replyText = returned.getReplyText();
                log.error("返回状态码：{}，描述信息：{}", replyCode, replyText);
                Message message = returned.getMessage();

            }
        });
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // ApplicationContextAware接口，在Spring容器初始化完成后，会通知实现了ApplicationContextAware接口的类，此时可以达到将初始化完成后的Spring容器传递进来
        // TODO：这里获取的rabbitTemplate和通过@Autowired注入的rabbitTemplate是同一个对象吧？
        RabbitTemplate rabbitTemplateB = applicationContext.getBean(RabbitTemplate.class);
        // rabbitTemplateB.setReturnsCallback(new RabbitTemplate.ReturnsCallback() {
        //     // 这是只有发送失败的情况下才会回调，而发送成功并不会输出。对应PublishReturn机制
        //     @Override
        //     public void returnedMessage(ReturnedMessage returned) {
        //         int replyCode = returned.getReplyCode();
        //         String replyText = returned.getReplyText();
        //         log.error("返回状态码：{}，描述信息：{}", replyCode, replyText);
        //         Message message = returned.getMessage();
        //
        //     }
        // });
        log.error("是否相同：{}", rabbitTemplateA == rabbitTemplateB);
    }
}
