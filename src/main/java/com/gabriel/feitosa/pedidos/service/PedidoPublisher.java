package com.gabriel.feitosa.pedidos.service;

import com.gabriel.feitosa.pedidos.model.Pedido;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PedidoPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbit.exchange}")
    private String exchange;

    @Value("${app.rabbit.routing-entrada}")
    private String routingKeyEntrada;

    public void publicar(Pedido pedido) {
        log.info("Publicando pedido na fila. id={}, produto={}, quantidade={}",
                pedido.getId(), pedido.getProduto(), pedido.getQuantidade());

        rabbitTemplate.convertAndSend(exchange, routingKeyEntrada, pedido);

        log.info("Pedido publicado com sucesso. id={}", pedido.getId());
    }
}
