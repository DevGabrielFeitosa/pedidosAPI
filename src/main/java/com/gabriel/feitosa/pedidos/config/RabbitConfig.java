package com.gabriel.feitosa.pedidos.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitConfig {

    @Bean
    public TopicExchange pedidosExchange(AppRabbitProperties properties) {
        return new TopicExchange(properties.getExchange());
    }

    @Bean
    public DirectExchange deadLetterExchange(AppRabbitProperties properties) {
        return new DirectExchange(properties.getDlx());
    }

    @Bean
    public Queue filaEntrada(AppRabbitProperties properties) {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", properties.getDlx());
        args.put("x-dead-letter-routing-key", properties.getRoutingDlq());

        return new Queue(properties.getQueueEntrada(), true, false, false, args);
    }

    @Bean
    public Queue filaDlq(AppRabbitProperties properties) {
        return new Queue(properties.getQueueDlq(), true);
    }

    @Bean
    public Queue filaStatusSucesso(AppRabbitProperties properties) {
        return new Queue(properties.getQueueStatusSucesso(), true);
    }

    @Bean
    public Queue filaStatusFalha(AppRabbitProperties properties) {
        return new Queue(properties.getQueueStatusFalha(), true);
    }

    @Bean
    public Binding bindingFilaEntrada(Queue filaEntrada,
                                      TopicExchange pedidosExchange,
                                      AppRabbitProperties properties) {
        return BindingBuilder.bind(filaEntrada)
                .to(pedidosExchange)
                .with(properties.getRoutingEntrada());
    }

    @Bean
    public Binding bindingFilaSucesso(Queue filaStatusSucesso,
                                      TopicExchange pedidosExchange,
                                      AppRabbitProperties properties) {
        return BindingBuilder.bind(filaStatusSucesso)
                .to(pedidosExchange)
                .with(properties.getRoutingStatusSucesso());
    }

    @Bean
    public Binding bindingFilaFalha(Queue filaStatusFalha,
                                    TopicExchange pedidosExchange,
                                    AppRabbitProperties properties) {
        return BindingBuilder.bind(filaStatusFalha)
                .to(pedidosExchange)
                .with(properties.getRoutingStatusFalha());
    }

    @Bean
    public Binding bindingFilaDlq(Queue filaDlq,
                                  DirectExchange deadLetterExchange,
                                  AppRabbitProperties properties) {
        return BindingBuilder.bind(filaDlq)
                .to(deadLetterExchange)
                .with(properties.getRoutingDlq());
    }

    @Bean
    public JacksonJsonMessageConverter jacksonJsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
