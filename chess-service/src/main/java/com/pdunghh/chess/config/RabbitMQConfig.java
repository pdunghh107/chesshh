package com.pdunghh.chess.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pdunghh.shared.event.RabbitMQConstants;

@Configuration
public class RabbitMQConfig {

    // Main Topic Exchange
    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(RabbitMQConstants.USER_EXCHANGE);
    }

    // Dead Letter Exchange (DLX)
    @Bean
    public DirectExchange userDlx() {
        return new DirectExchange(RabbitMQConstants.USER_DLX);
    }

    // Dead Letter Queue (DLQ)
    @Bean
    public Queue playerStatsUserRegisteredDlq() {
        return QueueBuilder.durable(RabbitMQConstants.CHESS_PLAYER_STATS_USER_REGISTERED_DLQ).build();
    }

    // DLQ Binding to DLX
    @Bean
    public Binding dlqBinding(Queue playerStatsUserRegisteredDlq, DirectExchange userDlx) {
        return BindingBuilder.bind(playerStatsUserRegisteredDlq)
                .to(userDlx)
                .with(RabbitMQConstants.USER_REGISTERED_DLQ_ROUTING_KEY);
    }

    // Main Queue with DLX configuration
    @Bean
    public Queue playerStatsUserRegisteredQueue() {
        return QueueBuilder.durable(RabbitMQConstants.CHESS_PLAYER_STATS_USER_REGISTERED_QUEUE)
                .withArgument("x-dead-letter-exchange", RabbitMQConstants.USER_DLX)
                .withArgument("x-dead-letter-routing-key", RabbitMQConstants.USER_REGISTERED_DLQ_ROUTING_KEY)
                .build();
    }

    // Main Binding to Main Exchange
    @Bean
    public Binding userRegisteredBinding(Queue playerStatsUserRegisteredQueue, TopicExchange userExchange) {
        return BindingBuilder.bind(playerStatsUserRegisteredQueue)
                .to(userExchange)
                .with(RabbitMQConstants.USER_REGISTERED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            MessageConverter jsonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        // Khi het retry attempts -> Chuyen sang DLQ thong qua
        // RejectAndDontRequeueRecoverer
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}
