package com.gabriel.feitosa.pedidos.service;

import com.gabriel.feitosa.pedidos.model.StatusEnum;
import com.gabriel.feitosa.pedidos.model.StatusPedido;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatusPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbit.exchange}")
    private String exchange;

    @Value("${app.rabbit.routing-status-sucesso}")
    private String routingSucesso;

    @Value("${app.rabbit.routing-status-falha}")
    private String routingFalha;

    public void publicarSucesso(UUID idPedido) {
        StatusPedido status = StatusPedido.builder()
                .idPedido(idPedido)
                .status(StatusEnum.SUCESSO)
                .dataProcessamento(LocalDateTime.now())
                .build();

        rabbitTemplate.convertAndSend(exchange, routingSucesso, status);

        log.info("Publicado status SUCESSO id={}", idPedido);
    }

    public void publicarFalha(UUID idPedido, String erro) {
        StatusPedido status = StatusPedido.builder()
                .idPedido(idPedido)
                .status(StatusEnum.FALHA)
                .dataProcessamento(LocalDateTime.now())
                .mensagemErro(erro)
                .build();

        rabbitTemplate.convertAndSend(exchange, routingFalha, status);

        log.info("Publicado status FALHA id={}", idPedido);
    }
}
