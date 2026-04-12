package com.tju.unify.conv.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置类
 * 职责：配置RabbitMQ队列、消息转换器等
 * 设计原则：单一职责原则 - 只负责RabbitMQ配置
 */
@Configuration
public class RabbitMQConfig {
    
    /**
     * 订单支付完成队列
     * 订单系统发送消息到此队列，营销系统监听此队列
     */
    public static final String ORDER_PAID_QUEUE = "order.paid.queue";
    
    /**
     * 创建订单支付完成队列
     * durable=true 表示队列持久化
     */
    @Bean
    public Queue orderPaidQueue() {
        return new Queue(ORDER_PAID_QUEUE, true);
    }
    
    /**
     * 配置消息转换器（JSON格式）
     * 设计原则：封装与抽象 - 封装消息序列化逻辑
     */
    @Bean
    public MessageConverter messageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        // 注册Java 8时间模块，支持LocalDateTime等类型
        objectMapper.registerModule(new JavaTimeModule());
        // 禁用将日期写为时间戳的特性
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        return new Jackson2JsonMessageConverter(objectMapper);
    }
    
    /**
     * 配置RabbitTemplate
     * 用于发送消息到队列
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
    
    /**
     * 配置消息监听器容器工厂
     * 用于接收消息
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        return factory;
    }
}

