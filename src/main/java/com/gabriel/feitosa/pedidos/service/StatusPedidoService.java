package com.gabriel.feitosa.pedidos.service;

import com.gabriel.feitosa.pedidos.model.StatusEnum;
import com.gabriel.feitosa.pedidos.model.StatusPedido;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class StatusPedidoService {

    private final Map<UUID, StatusPedido> statusMap = new ConcurrentHashMap<>();

    public void salvarStatusInicial(UUID idPedido) {
        StatusPedido statusPedido = StatusPedido.builder()
                .idPedido(idPedido)
                .status(StatusEnum.RECEBIDO)
                .dataProcessamento(null)
                .mensagemErro(null)
                .build();

        statusMap.put(idPedido, statusPedido);
    }

    public void atualizarParaProcessando(UUID idPedido) {
        StatusPedido atual = buscarPorId(idPedido);

        atual.setStatus(StatusEnum.PROCESSANDO);
        statusMap.put(idPedido, atual);
    }

    public void atualizarParaSucesso(UUID idPedido) {
        StatusPedido atual = buscarPorId(idPedido);

        atual.setStatus(StatusEnum.SUCESSO);
        atual.setDataProcessamento(java.time.LocalDateTime.now());
        atual.setMensagemErro(null);

        statusMap.put(idPedido, atual);
    }

    public void atualizarParaFalha(UUID idPedido, String mensagemErro) {
        StatusPedido atual = buscarPorId(idPedido);

        atual.setStatus(StatusEnum.FALHA);
        atual.setDataProcessamento(java.time.LocalDateTime.now());
        atual.setMensagemErro(mensagemErro);

        statusMap.put(idPedido, atual);
    }

    public StatusPedido buscarPorId(UUID idPedido) {
        StatusPedido statusPedido = statusMap.get(idPedido);

        if (statusPedido == null) {
            throw new RuntimeException("Pedido não encontrado para o id: " + idPedido);
        }

        return statusPedido;
    }
}
