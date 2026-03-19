package com.gabriel.feitosa.pedidos.desktop.dto;

import com.gabriel.feitosa.pedidos.desktop.model.PedidoStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusPedidoResponseDto {

    private UUID idPedido;
    private PedidoStatus status;
    private LocalDateTime dataProcessamento;
    private String mensagemErro;
}