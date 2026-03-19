package com.gabriel.feitosa.pedidos.exception;

public class ProcessamentoPedidoException extends RuntimeException {

    public ProcessamentoPedidoException(String message) {
        super(message);
    }

    public ProcessamentoPedidoException(String message, Throwable cause) {
        super(message, cause);
    }
}