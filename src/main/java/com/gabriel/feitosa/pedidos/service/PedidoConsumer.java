package com.gabriel.feitosa.pedidos.service;


import com.gabriel.feitosa.pedidos.exception.ProcessamentoPedidoException;
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
            statusPedidoService.atualizarParaProcessando(pedido.getId());

            int tempo = 1000 + random.nextInt(2001);
            Thread.sleep(tempo);

            if (random.nextDouble() < 0.2) {
                throw new ProcessamentoPedidoException("Erro simulado no processamento");
            }

            statusPedidoService.atualizarParaSucesso(pedido.getId());
            statusPublisher.publicarSucesso(pedido.getId());

            log.info("Pedido processado com SUCESSO id={}", pedido.getId());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            tratarFalha(pedido, new ProcessamentoPedidoException("Processamento interrompido", e));

        } catch (ProcessamentoPedidoException e) {
            tratarFalha(pedido, e);

        } catch (Exception e) {
            tratarFalha(pedido, new ProcessamentoPedidoException("Erro inesperado no processamento", e));
        }
    }

    private void tratarFalha(Pedido pedido, ProcessamentoPedidoException e) {
        log.error("Erro ao processar pedido id={}", pedido.getId(), e);
        statusPedidoService.atualizarParaFalha(pedido.getId(), e.getMessage());
        statusPublisher.publicarFalha(pedido.getId(), e.getMessage());
        throw new AmqpRejectAndDontRequeueException(e.getMessage(), e);
    }
}
