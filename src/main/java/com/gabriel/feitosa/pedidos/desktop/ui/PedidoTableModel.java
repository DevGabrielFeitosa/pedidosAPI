package com.gabriel.feitosa.pedidos.desktop.ui;

import com.gabriel.feitosa.pedidos.desktop.dto.StatusPedidoResponseDto;
import com.gabriel.feitosa.pedidos.desktop.model.PedidoStatus;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PedidoTableModel extends AbstractTableModel {

    private static final String[] COLUNAS = {"ID", "Produto", "Quantidade", "Status", "Mensagem"};

    private final List<LinhaPedido> linhas = new ArrayList<>();
    private final Map<UUID, Integer> indicesPorId = new HashMap<>();

    public void adicionarPedido(UUID id, String produto, int quantidade, String status, String mensagem) {
        LinhaPedido linha = new LinhaPedido(id, produto, quantidade, status, mensagem);
        linhas.add(linha);
        indicesPorId.put(id, linhas.size() - 1);
        fireTableRowsInserted(linhas.size() - 1, linhas.size() - 1);
    }

    public void atualizarStatus(StatusPedidoResponseDto response) {
        Integer indice = indicesPorId.get(response.getIdPedido());

        if (indice == null) {
            return;
        }

        LinhaPedido linha = linhas.get(indice);
        linha.setStatus(response.getStatus().name());
        linha.setMensagem(mensagemStatus(response));
        fireTableRowsUpdated(indice, indice);
    }

    public String getStatus(UUID id) {
        Integer indice = indicesPorId.get(id);
        return indice == null ? null : linhas.get(indice).getStatus();
    }

    public String getMensagem(UUID id) {
        Integer indice = indicesPorId.get(id);
        return indice == null ? null : linhas.get(indice).getMensagem();
    }

    @Override
    public int getRowCount() {
        return linhas.size();
    }

    @Override
    public int getColumnCount() {
        return COLUNAS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUNAS[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        LinhaPedido linha = linhas.get(rowIndex);

        return switch (columnIndex) {
            case 0 -> linha.getId();
            case 1 -> linha.getProduto();
            case 2 -> linha.getQuantidade();
            case 3 -> linha.getStatus();
            case 4 -> linha.getMensagem();
            default -> "";
        };
    }

    private String mensagemStatus(StatusPedidoResponseDto response) {
        if (response.getStatus() == PedidoStatus.FALHA) {
            return response.getMensagemErro() == null || response.getMensagemErro().isBlank()
                    ? "Falha ao processar pedido"
                    : response.getMensagemErro();
        }

        if (response.getStatus() == PedidoStatus.SUCESSO) {
            return "Pedido processado com sucesso";
        }

        if (response.getStatus() == PedidoStatus.PROCESSANDO) {
            return "Pedido em processamento";
        }

        return "Pedido recebido e aguardando processamento";
    }

    private static class LinhaPedido {
        private final UUID id;
        private final String produto;
        private final int quantidade;
        private String status;
        private String mensagem;

        private LinhaPedido(UUID id, String produto, int quantidade, String status, String mensagem) {
            this.id = id;
            this.produto = produto;
            this.quantidade = quantidade;
            this.status = status;
            this.mensagem = mensagem;
        }

        public UUID getId() {
            return id;
        }

        public String getProduto() {
            return produto;
        }

        public int getQuantidade() {
            return quantidade;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getMensagem() {
            return mensagem;
        }

        public void setMensagem(String mensagem) {
            this.mensagem = mensagem;
        }
    }
}