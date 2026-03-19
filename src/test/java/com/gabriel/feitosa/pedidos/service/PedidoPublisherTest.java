package com.gabriel.feitosa.pedidos.service;

import com.gabriel.feitosa.pedidos.model.Pedido;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PedidoPublisherTest {

    @Test
    void devePublicarPedidoNaExchangeComRoutingKeyConfiguradas() {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        PedidoPublisher publisher = new PedidoPublisher(rabbitTemplate);

        ReflectionTestUtils.setField(publisher, "exchange", "pedidos.exchange.gabriel");
        ReflectionTestUtils.setField(publisher, "routingKeyEntrada", "pedidos.entrada");

        Pedido pedido = Pedido.builder()
                .id(UUID.randomUUID())
                .produto("Notebook")
                .quantidade(2)
                .dataCriacao(LocalDateTime.now())
                .build();

        publisher.publicar(pedido);

        verify(rabbitTemplate).convertAndSend("pedidos.exchange.gabriel", "pedidos.entrada", pedido);
    }
}