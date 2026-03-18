package com.gabriel.feitosa.pedidos.dto;

import com.gabriel.feitosa.pedidos.model.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class StatusPedidoResponse {

    private UUID idPedido;
    private StatusEnum status;
    private LocalDateTime dataProcessamento;
    private String mensagemErro;
}
