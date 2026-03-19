package com.gabriel.feitosa.pedidos.desktop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoRequestDto {

    private UUID id;
    private String produto;
    private int quantidade;
    private LocalDateTime dataCriacao;
}