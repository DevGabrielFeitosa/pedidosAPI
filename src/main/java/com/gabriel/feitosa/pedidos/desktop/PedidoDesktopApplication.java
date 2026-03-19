package com.gabriel.feitosa.pedidos.desktop;

import com.gabriel.feitosa.pedidos.desktop.client.PedidoHttpClient;
import com.gabriel.feitosa.pedidos.desktop.ui.PedidoFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class PedidoDesktopApplication {

    public static void main(String[] args) {
        configurarLookAndFeel();

        String backendUrl = System.getProperty(
                "pedidos.backend.url",
                System.getenv().getOrDefault("PEDIDOS_BACKEND_URL", "http://localhost:8080")
        );

        SwingUtilities.invokeLater(() -> new PedidoFrame(new PedidoHttpClient(backendUrl)).setVisible(true));
    }

    private static void configurarLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
    }
}