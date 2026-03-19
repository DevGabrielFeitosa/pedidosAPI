package com.gabriel.feitosa.pedidos.desktop.client;

public class DesktopClientException extends RuntimeException {

    public DesktopClientException(String message) {
        super(message);
    }

    public DesktopClientException(String message, Throwable cause) {
        super(message, cause);
    }
}