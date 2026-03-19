package com.gabriel.feitosa.pedidos.desktop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoResponseDto {

    private UUID id;
    private String mensagem;
}