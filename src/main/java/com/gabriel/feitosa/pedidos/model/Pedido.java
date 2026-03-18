package com.gabriel.feitosa.pedidos.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pedido {

    private UUID id;
    private String produto;
    private int quantidade;
    private LocalDateTime dataCriacao;
}
