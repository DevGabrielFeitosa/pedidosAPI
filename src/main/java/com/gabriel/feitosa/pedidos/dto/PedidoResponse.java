package com.gabriel.feitosa.pedidos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class PedidoResponse {

    private UUID id;
    private String mensagem;
}
