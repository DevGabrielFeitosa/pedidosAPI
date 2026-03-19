package com.gabriel.feitosa.pedidos.exception;

public class PedidoNotFoundException extends RuntimeException {

    public PedidoNotFoundException(String message) {
        super(message);
    }
}