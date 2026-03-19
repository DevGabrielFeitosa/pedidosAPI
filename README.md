# Sistema de Pedidos Assíncrono

Projeto do desafio técnico com **backend Spring Boot** e **cliente desktop em Java Swing** para processamento assíncrono de pedidos com **RabbitMQ**.

A aplicação recebe pedidos via HTTP, coloca a mensagem em uma fila RabbitMQ e faz o processamento de forma assíncrona. Durante o processamento, o status do pedido é mantido em memória e pode ser consultado por endpoint.

## Tecnologias usadas

- Java 17
- Spring Boot
- Spring Web
- Spring AMQP
- RabbitMQ
- Maven
- JUnit / Mockito

## O que foi implementado

- `POST /api/pedidos` para receber novos pedidos
- validação de payload (`produto` obrigatório, `quantidade > 0`)
- publicação do pedido na fila de entrada
- consumidor assíncrono com `@RabbitListener`
- simulação de processamento com tempo aleatório
- falha simulada com envio para DLQ
- publicação de status de sucesso e falha em filas separadas
- controle de status em memória
- `GET /api/pedidos/status/{id}` para consulta do pedido
- teste unitário do publisher responsável por enviar a mensagem para a fila

## Estrutura das filas

As filas configuradas no projeto são:

- `pedidos.entrada.gabriel`
- `pedidos.entrada.gabriel.dlq`
- `pedidos.status.sucesso.gabriel`
- `pedidos.status.falha.gabriel`

## Como funciona o fluxo

1. O cliente envia um pedido para a API.
2. O backend valida os dados e responde com `202 Accepted`.
3. O pedido é publicado na fila de entrada.
4. Um consumidor lê a mensagem e simula o processamento.
5. Em caso de sucesso, publica um status de sucesso.
6. Em caso de falha, publica um status de falha e rejeita a mensagem para seguir para a DLQ.
7. O status também fica disponível para consulta pelo endpoint HTTP.

## Exemplo de payload

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "produto": "Notebook",
  "quantidade": 2,
  "dataCriacao": "2026-03-18T20:00:00"
}
```

## Endpoints

### Criar pedido

`POST /api/pedidos`

Resposta esperada:

- `202 Accepted` em caso de sucesso
- `400 Bad Request` em caso de payload inválido

### Consultar status

`GET /api/pedidos/status/{id}`

Resposta esperada:

- `200 OK` quando o pedido existe
- `404 Not Found` quando o pedido não foi encontrado

## Como rodar o projeto

### 1. Subir um RabbitMQ local

Se quiser rodar localmente com Docker:

```bash
docker run -d --hostname rabbit-local --name rabbitmq \
  -p 5672:5672 -p 15672:15672 \
  rabbitmq:3-management
```

O projeto está configurado por padrão para usar:

- host: `localhost`
- port: `5672`
- user: `guest`
- password: `guest`

Painel do RabbitMQ:

- `http://localhost:15672`

### 2. Rodar o backend

No diretório do projeto:

```bash
./mvnw spring-boot:run
```

No Windows:

```bash
.\mvnw.cmd spring-boot:run
```

### 3. Rodar a aplicação desktop Swing

A aplicação desktop está no pacote:

- `com.gabriel.feitosa.pedidos.desktop`

Classe principal:

- `com.gabriel.feitosa.pedidos.desktop.PedidoDesktopApplication`

O jeito mais simples de executar é pela IDE, rodando essa classe `main`.

Se quiser apontar o desktop para outra URL do backend, use uma das opções abaixo:

- VM option:
  - `-Dpedidos.backend.url=http://localhost:8080`
- variável de ambiente:
  - `PEDIDOS_BACKEND_URL=http://localhost:8080`

Fluxo esperado para demonstração:

1. subir o backend
2. abrir a aplicação Swing
3. enviar um pedido pela interface
4. acompanhar a mudança de status na tabela até `SUCESSO` ou `FALHA`

## Rodando os testes

```bash
./mvnw test
```

No Windows:

```bash
.\mvnw.cmd test
```

## Observações

- O status dos pedidos é mantido em memória, sem banco de dados.
- A DLQ foi configurada para demonstrar o tratamento de falhas no consumo.
- A interface Swing envia pedidos por HTTP e faz polling assíncrono de status sem bloquear a UI.

## Melhorias que poderiam ser feitas

- adicionar testes para controller e service
- externalizar mais a configuração do RabbitMQ
- persistir status em banco caso o sistema evolua

---

Projeto desenvolvido para fins de avaliação técnica.