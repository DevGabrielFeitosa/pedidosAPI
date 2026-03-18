package com.gabriel.feitosa.pedidos.controller;

import com.gabriel.feitosa.pedidos.dto.PedidoRequest;
import com.gabriel.feitosa.pedidos.dto.PedidoResponse;
import com.gabriel.feitosa.pedidos.dto.StatusPedidoResponse;
import com.gabriel.feitosa.pedidos.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping
    public ResponseEntity<PedidoResponse> criarPedido(@Valid @RequestBody PedidoRequest request) {
        PedidoResponse response = pedidoService.criarPedido(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/status/{id}")
    public ResponseEntity<StatusPedidoResponse> consultarStatus(@PathVariable UUID id) {
        StatusPedidoResponse response = pedidoService.consultarStatus(id);
        return ResponseEntity.ok(response);
    }
}
