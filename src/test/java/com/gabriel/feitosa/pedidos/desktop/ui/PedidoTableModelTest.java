package com.gabriel.feitosa.pedidos.desktop.ui;

import com.gabriel.feitosa.pedidos.desktop.dto.StatusPedidoResponseDto;
import com.gabriel.feitosa.pedidos.desktop.model.PedidoStatus;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PedidoTableModelTest {

    @Test
    void deveAtualizarLinhaParaSucesso() {
        PedidoTableModel model = new PedidoTableModel();
        UUID id = UUID.randomUUID();

        model.adicionarPedido(id, "Notebook", 2, PedidoStatus.RECEBIDO.name(), "Pedido recebido");
        model.atualizarStatus(new StatusPedidoResponseDto(id, PedidoStatus.SUCESSO, null, null));

        assertEquals(1, model.getRowCount());
        assertEquals("SUCESSO", model.getStatus(id));
        assertEquals("Pedido processado com sucesso", model.getMensagem(id));
    }

    @Test
    void deveAtualizarLinhaParaFalhaComMensagemDoBackend() {
        PedidoTableModel model = new PedidoTableModel();
        UUID id = UUID.randomUUID();

        model.adicionarPedido(id, "Mouse", 1, PedidoStatus.RECEBIDO.name(), "Pedido recebido");
        model.atualizarStatus(new StatusPedidoResponseDto(id, PedidoStatus.FALHA, null, "Erro simulado no processamento"));

        assertEquals("FALHA", model.getStatus(id));
        assertEquals("Erro simulado no processamento", model.getMensagem(id));
    }
}