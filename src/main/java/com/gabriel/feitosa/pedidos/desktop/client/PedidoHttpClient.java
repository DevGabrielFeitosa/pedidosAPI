package com.gabriel.feitosa.pedidos.desktop.client;

import com.gabriel.feitosa.pedidos.desktop.dto.PedidoRequestDto;
import com.gabriel.feitosa.pedidos.desktop.dto.PedidoResponseDto;
import com.gabriel.feitosa.pedidos.desktop.dto.StatusPedidoResponseDto;
import com.gabriel.feitosa.pedidos.desktop.model.PedidoStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PedidoHttpClient {

    private final HttpClient httpClient;
    private final String baseUrl;

    public PedidoHttpClient(String baseUrl) {
        this(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build(),
                normalizarBaseUrl(baseUrl)
        );
    }

    PedidoHttpClient(HttpClient httpClient, String baseUrl) {
        this.httpClient = httpClient;
        this.baseUrl = normalizarBaseUrl(baseUrl);
    }

    public PedidoResponseDto criarPedido(PedidoRequestDto request) {
        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(baseUrl + "/api/pedidos"))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.ofString(serializar(request)))
                .build();

        return executar(httpRequest, 202, PedidoResponseDto.class);
    }

    public StatusPedidoResponseDto consultarStatus(UUID idPedido) {
        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(baseUrl + "/api/pedidos/status/" + idPedido))
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        return executar(httpRequest, 200, StatusPedidoResponseDto.class);
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    private <T> T executar(HttpRequest request, int statusEsperado, Class<T> responseType) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != statusEsperado) {
                throw new DesktopClientException(montarMensagemErro(response));
            }

            return desserializar(response.body(), responseType);
        } catch (IOException e) {
            throw new DesktopClientException("Não foi possível se comunicar com o backend.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DesktopClientException("A operação foi interrompida durante a comunicação com o backend.", e);
        }
    }

    private String serializar(Object body) {
        if (body instanceof PedidoRequestDto request) {
            return "{" +
                    "\"id\":\"" + request.getId() + "\"," +
                    "\"produto\":\"" + escaparJson(request.getProduto()) + "\"," +
                    "\"quantidade\":" + request.getQuantidade() + "," +
                    "\"dataCriacao\":\"" + request.getDataCriacao() + "\"" +
                    "}";
        }

        throw new DesktopClientException("Não foi possível gerar o JSON do pedido.");
    }

    private String montarMensagemErro(HttpResponse<String> response) {
        String corpo = response.body();

        String mensagem = extrairCampoTexto(corpo, "erro");
        String detalhe = extrairCampoTexto(corpo, "detalhe");

        if (mensagem != null && detalhe != null) {
            return mensagem + ": " + detalhe;
        }

        if (mensagem != null) {
            return mensagem;
        }

        if (corpo != null && !corpo.isBlank()) {
            return "Erro ao acessar backend (HTTP " + response.statusCode() + "): " + corpo;
        }

        return "Erro ao acessar backend. HTTP " + response.statusCode();
    }

    @SuppressWarnings("unchecked")
    private <T> T desserializar(String body, Class<T> responseType) {
        if (responseType == PedidoResponseDto.class) {
            return (T) new PedidoResponseDto(
                    extrairUuid(body, "id"),
                    extrairCampoTexto(body, "mensagem")
            );
        }

        if (responseType == StatusPedidoResponseDto.class) {
            String dataProcessamento = extrairCampoTexto(body, "dataProcessamento");

            return (T) new StatusPedidoResponseDto(
                    extrairUuid(body, "idPedido"),
                    PedidoStatus.valueOf(extrairCampoTexto(body, "status")),
                    dataProcessamento == null ? null : LocalDateTime.parse(dataProcessamento),
                    extrairCampoTexto(body, "mensagemErro")
            );
        }

        throw new DesktopClientException("Tipo de resposta não suportado pelo cliente desktop.");
    }

    private UUID extrairUuid(String body, String campo) {
        String valor = extrairCampoTexto(body, campo);
        if (valor == null) {
            throw new DesktopClientException("Campo obrigatório ausente na resposta: " + campo);
        }
        return UUID.fromString(valor);
    }

    private String extrairCampoTexto(String body, String campo) {
        if (body == null || body.isBlank()) {
            return null;
        }

        Pattern pattern = Pattern.compile("\\\"" + Pattern.quote(campo) + "\\\"\\s*:\\s*(null|\\\"((?:\\\\.|[^\\\"])*)\\\")");
        Matcher matcher = pattern.matcher(body);

        if (!matcher.find()) {
            return null;
        }

        if ("null".equals(matcher.group(1))) {
            return null;
        }

        return desescaparJson(matcher.group(2));
    }

    private String escaparJson(String valor) {
        return valor
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }

    private String desescaparJson(String valor) {
        return valor
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    private static String normalizarBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "http://localhost:8080";
        }

        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}