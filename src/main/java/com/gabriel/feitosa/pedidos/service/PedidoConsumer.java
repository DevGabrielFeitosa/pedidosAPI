package com.gabriel.feitosa.pedidos.service;


import com.gabriel.feitosa.pedidos.model.Pedido;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
public class PedidoConsumer {

    private final StatusPedidoService statusPedidoService;
    private final StatusPublisher statusPublisher;

    private final Random random = new Random();

    @RabbitListener(queues = "${app.rabbit.queue-entrada}")
    public void consumir(Pedido pedido) {

        log.info("Iniciando processamento do pedido id={}", pedido.getId());

        try {
            // Atualiza status
            statusPedidoService.atualizarParaProcessando(pedido.getId());

            // Simula processamento (1 a 3 segundos)
            int tempo = 1000 + random.nextInt(2000);
            Thread.sleep(tempo);

            // 20% chance de falha
            if (random.nextDouble() < 0.2) {
                throw new RuntimeException("Erro simulado no processamento");
            }

            // Sucesso
            statusPedidoService.atualizarParaSucesso(pedido.getId());

            statusPublisher.publicarSucesso(pedido.getId());

            log.info("Pedido processado com SUCESSO id={}", pedido.getId());

        } catch (Exception e) {

            log.error("Erro ao processar pedido id={}", pedido.getId(), e);

            statusPedidoService.atualizarParaFalha(pedido.getId(), e.getMessage());

            statusPublisher.publicarFalha(pedido.getId(), e.getMessage());

            throw new AmqpRejectAndDontRequeueException(e.getMessage());
        }
    }
}
