package com.gabriel.feitosa.pedidos.desktop.ui;

import com.gabriel.feitosa.pedidos.desktop.client.PedidoHttpClient;
import com.gabriel.feitosa.pedidos.desktop.dto.PedidoRequestDto;
import com.gabriel.feitosa.pedidos.desktop.dto.PedidoResponseDto;
import com.gabriel.feitosa.pedidos.desktop.dto.StatusPedidoResponseDto;
import com.gabriel.feitosa.pedidos.desktop.model.PedidoStatus;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class PedidoFrame extends JFrame {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final PedidoHttpClient pedidoHttpClient;
    private final PedidoTableModel tableModel = new PedidoTableModel();
    private final Set<UUID> pedidosPendentes = new LinkedHashSet<>();

    private final JTextField produtoField = new JTextField(20);
    private final JTextField quantidadeField = new JTextField(6);
    private final JButton enviarButton = new JButton("Enviar pedido");
    private final JLabel statusLabel = new JLabel();

    private boolean consultaEmAndamento;
    private Timer pollingTimer;

    public PedidoFrame(PedidoHttpClient pedidoHttpClient) {
        this.pedidoHttpClient = pedidoHttpClient;

        setTitle("Sistema de Pedidos Desktop");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 500));
        setLocationRelativeTo(null);

        inicializarComponentes();
        inicializarPolling();
        atualizarRodape("Conectado ao backend em " + pedidoHttpClient.getBaseUrl());
    }

    private void inicializarComponentes() {
        setLayout(new BorderLayout(10, 10));

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        formulario.add(new JLabel("Produto:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        formulario.add(produtoField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formulario.add(new JLabel("Quantidade:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formulario.add(quantidadeField, gbc);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        botoes.add(enviarButton);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        formulario.add(botoes, gbc);

        JTable pedidosTable = new JTable(tableModel);
        pedidosTable.setFillsViewportHeight(true);
        pedidosTable.setRowHeight(24);

        JScrollPane scrollPane = new JScrollPane(pedidosTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Pedidos enviados"));

        statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 12, 12, 12));

        add(formulario, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        enviarButton.addActionListener(e -> enviarPedido());
    }

    private void inicializarPolling() {
        pollingTimer = new Timer(2000, e -> consultarPedidosPendentes());
        pollingTimer.setInitialDelay(2000);
        pollingTimer.start();
    }

    private void enviarPedido() {
        String produto = produtoField.getText().trim();

        if (produto.isBlank()) {
            mostrarErro("Informe o produto.");
            return;
        }

        int quantidade;

        try {
            quantidade = Integer.parseInt(quantidadeField.getText().trim());
        } catch (NumberFormatException e) {
            mostrarErro("Informe uma quantidade numérica válida.");
            return;
        }

        if (quantidade <= 0) {
            mostrarErro("A quantidade deve ser maior que zero.");
            return;
        }

        PedidoRequestDto request = new PedidoRequestDto(UUID.randomUUID(), produto, quantidade, LocalDateTime.now());

        enviarButton.setEnabled(false);
        atualizarRodape("Enviando pedido para processamento...");

        new SwingWorker<PedidoResponseDto, Void>() {
            @Override
            protected PedidoResponseDto doInBackground() {
                return pedidoHttpClient.criarPedido(request);
            }

            @Override
            protected void done() {
                enviarButton.setEnabled(true);

                try {
                    PedidoResponseDto response = get();
                    tableModel.adicionarPedido(
                            response.getId(),
                            request.getProduto(),
                            request.getQuantidade(),
                            PedidoStatus.RECEBIDO.name(),
                            response.getMensagem()
                    );
                    pedidosPendentes.add(response.getId());
                    limparFormulario();
                    atualizarRodape("Pedido enviado com sucesso às " + LocalTime.now().format(TIME_FORMATTER));
                    consultarPedidosPendentes();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    mostrarErro("O envio do pedido foi interrompido.");
                } catch (ExecutionException e) {
                    mostrarErro(extrairMensagem(e));
                }
            }
        }.execute();
    }

    private void consultarPedidosPendentes() {
        if (consultaEmAndamento || pedidosPendentes.isEmpty()) {
            return;
        }

        consultaEmAndamento = true;
        List<UUID> ids = new ArrayList<>(pedidosPendentes);

        new SwingWorker<ConsultaResultado, Void>() {
            @Override
            protected ConsultaResultado doInBackground() {
                Map<UUID, StatusPedidoResponseDto> atualizacoes = new LinkedHashMap<>();
                List<String> erros = new ArrayList<>();

                for (UUID id : ids) {
                    try {
                        atualizacoes.put(id, pedidoHttpClient.consultarStatus(id));
                    } catch (Exception e) {
                        erros.add("Falha ao consultar pedido " + id + ": " + extrairMensagem(e));
                    }
                }

                return new ConsultaResultado(atualizacoes, erros);
            }

            @Override
            protected void done() {
                consultaEmAndamento = false;

                try {
                    ConsultaResultado resultado = get();

                    for (StatusPedidoResponseDto response : resultado.atualizacoes().values()) {
                        tableModel.atualizarStatus(response);

                        if (response.getStatus() == PedidoStatus.SUCESSO || response.getStatus() == PedidoStatus.FALHA) {
                            pedidosPendentes.remove(response.getIdPedido());
                        }
                    }

                    if (!resultado.erros().isEmpty()) {
                        atualizarRodape(resultado.erros().get(0));
                    } else if (!resultado.atualizacoes().isEmpty()) {
                        atualizarRodape("Última atualização às " + LocalTime.now().format(TIME_FORMATTER));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    atualizarRodape("Consulta de status interrompida.");
                } catch (ExecutionException e) {
                    atualizarRodape(extrairMensagem(e));
                }
            }
        }.execute();
    }

    private void limparFormulario() {
        produtoField.setText("");
        quantidadeField.setText("");
        produtoField.requestFocusInWindow();
    }

    private void mostrarErro(String mensagem) {
        atualizarRodape(mensagem);
        JOptionPane.showMessageDialog(this, mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    private void atualizarRodape(String mensagem) {
        statusLabel.setText(mensagem);
    }

    private String extrairMensagem(Throwable throwable) {
        Throwable causa = throwable;

        if (throwable instanceof ExecutionException executionException && executionException.getCause() != null) {
            causa = executionException.getCause();
        }

        return causa.getMessage() == null || causa.getMessage().isBlank()
                ? "Ocorreu um erro inesperado."
                : causa.getMessage();
    }

    private record ConsultaResultado(Map<UUID, StatusPedidoResponseDto> atualizacoes, List<String> erros) {
    }
}