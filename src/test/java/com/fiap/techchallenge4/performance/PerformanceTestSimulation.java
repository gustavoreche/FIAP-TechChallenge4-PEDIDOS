package com.fiap.techchallenge4.performance;

import io.gatling.javaapi.core.ActionBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;


public class PerformanceTestSimulation extends Simulation {

    private final JdbcTemplate jdbcTemplate = this.criaConexaoComBaseDeDados();
    private final ClientAndServer mockServerProduto = this.criaMockServerProduto();
    private final ClientAndServer mockServerCliente = this.criaMockServerCliente();
    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8081");

    ActionBuilder criaPedidoRequest = http("cria pedido")
            .post("/pedido")
            .header("Content-Type", "application/json")
            .body(StringBody("""
                              {
                                "ean": 123,
                                "cpfCliente": "71622958004",
                                "quantidade": 1
                              }
                    """))
            .check(status().is(201));

    ActionBuilder cancelaPedidoRequest = http("cancela pedido")
            .delete("/pedido/${idPedido}")
            .header("Content-Type", "application/json")
            .check(status().is(200));

    ScenarioBuilder cenarioCriaPedido = scenario("Cria pedido")
            .exec(criaPedidoRequest);

    ScenarioBuilder cenarioCancelaPedido = scenario("Cancela pedido")
            .exec(session -> {
                long idPedido = System.currentTimeMillis();

                jdbcTemplate.execute("""
                INSERT INTO tb_pedido (id, cpf_cliente,data_de_criacao,ean,quantidade,status_pedido) VALUES
                	 (%s,'71622958004','2024-06-26 22:57:46.037',7894900011517,30,'CRIADO');
                """
                        .formatted(idPedido));

                return session.set("idPedido", idPedido);
            })
            .exec(cancelaPedidoRequest);


    {

        setUp(
                cenarioCriaPedido.injectOpen(
                        rampUsersPerSec(1)
                                .to(10)
                                .during(Duration.ofSeconds(10)),
                        constantUsersPerSec(10)
                                .during(Duration.ofSeconds(20)),
                        rampUsersPerSec(10)
                                .to(1)
                                .during(Duration.ofSeconds(10))),
                cenarioCancelaPedido.injectOpen(
                        rampUsersPerSec(1)
                                .to(10)
                                .during(Duration.ofSeconds(10)),
                        constantUsersPerSec(10)
                                .during(Duration.ofSeconds(20)),
                        rampUsersPerSec(10)
                                .to(1)
                                .during(Duration.ofSeconds(10)))
        )
                .protocols(httpProtocol)
                .assertions(
                        global().responseTime().max().lt(600),
                        global().failedRequests().count().is(0L));

    }


    private JdbcTemplate criaConexaoComBaseDeDados() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl("jdbc:postgresql://localhost:5434/tech_challenge_4_pedidos");
        dataSource.setUsername("root");
        dataSource.setPassword("root");
        return new JdbcTemplate(dataSource);
    }

    private ClientAndServer criaMockServerProduto() {
        final var clientAndServer = ClientAndServer.startClientAndServer(8080);

        clientAndServer.when(
               HttpRequest.request()
                       .withMethod("GET")
                       .withPath("/produto/estoque/123/1")
                )
                .respond(
                        HttpResponse.response()
                                .withContentType(MediaType.APPLICATION_JSON)
                                .withStatusCode(200)
                                .withBody("true")
                );

        return clientAndServer;
    }

    private ClientAndServer criaMockServerCliente() {
        final var clientAndServer = ClientAndServer.startClientAndServer(8083);

        clientAndServer.when(
                        HttpRequest.request()
                                .withMethod("GET")
                                .withPath("/cliente/71622958004")
                )
                .respond(
                        HttpResponse.response()
                                .withContentType(MediaType.APPLICATION_JSON)
                                .withStatusCode(200)
                                .withBody("""
                                            {
                                                "cpf": "71622958004",
                                                "nome": "Cliente Teste",
                                                "enderecoLogradouro": "Rua Teste",
                                                "enderecoNumero": 123,
                                                "enderecoSiglaEstado": "SP",
                                                "dataDeCriacao": "2021-10-10T10:00:00"
                                            }
                                        """)
                );

        return clientAndServer;
    }

}