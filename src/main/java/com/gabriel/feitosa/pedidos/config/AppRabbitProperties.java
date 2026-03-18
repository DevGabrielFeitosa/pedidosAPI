package com.gabriel.feitosa.pedidos.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.rabbit")
public class AppRabbitProperties {

    private String exchange;
    private String dlx;
    private String queueEntrada;
    private String queueDlq;
    private String queueStatusSucesso;
    private String queueStatusFalha;
    private String routingEntrada;
    private String routingStatusSucesso;
    private String routingStatusFalha;
    private String routingDlq;
}
