package com.gabriel.feitosa.pedidos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PedidoRequest {

    @NotNull
    private UUID id;

    @NotBlank
    private String produto;

    @Min(1)
    private int quantidade;

    @NotNull
    private LocalDateTime dataCriacao;
}
