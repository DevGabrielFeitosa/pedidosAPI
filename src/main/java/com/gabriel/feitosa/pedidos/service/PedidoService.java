package com.gabriel.feitosa.pedidos.service;

import com.gabriel.feitosa.pedidos.dto.PedidoRequest;
import com.gabriel.feitosa.pedidos.dto.PedidoResponse;
import com.gabriel.feitosa.pedidos.dto.StatusPedidoResponse;
import com.gabriel.feitosa.pedidos.model.Pedido;
import com.gabriel.feitosa.pedidos.model.StatusEnum;
import com.gabriel.feitosa.pedidos.model.StatusPedido;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoPublisher pedidoPublisher;
    private final StatusPedidoService statusPedidoService;

    public PedidoResponse criarPedido(PedidoRequest request) {
        Pedido pedido = Pedido.builder()
                .id(request.getId())
                .produto(request.getProduto())
                .quantidade(request.getQuantidade())
                .dataCriacao(request.getDataCriacao())
                .build();

        statusPedidoService.salvarStatusInicial(pedido.getId());

        pedidoPublisher.publicar(pedido);

        return new PedidoResponse(
                pedido.getId(),
                "Pedido recebido e será processado assincronamente"
        );
    }

    public StatusPedidoResponse consultarStatus(UUID id) {
        StatusPedido statusPedido = statusPedidoService.buscarPorId(id);

        return new StatusPedidoResponse(
                statusPedido.getIdPedido(),
                statusPedido.getStatus(),
                statusPedido.getDataProcessamento(),
                statusPedido.getMensagemErro()
        );
    }
}
