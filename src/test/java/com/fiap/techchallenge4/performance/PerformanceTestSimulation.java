package com.fiap.techchallenge4.performance;

import io.gatling.javaapi.core.ActionBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class PerformanceTestSimulation extends Simulation {

    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8081");

    ActionBuilder criaPedidoRequest = http("cria pedido")
            .post("/pedido")
            .header("Content-Type", "application/json")
            .body(StringBody("""
                              {
                                "ean": ${ean},
                                "cpfCliente": "71622958004",
                                "quantidade": 1
                              }
                    """))
            .check(status().is(201));

    ScenarioBuilder cenarioCriaPedido = scenario("Cria pedido")
            .exec(session -> {
                long ean = System.currentTimeMillis();
                return session.set("ean", ean);
            })
            .exec(criaPedidoRequest);


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
                                .during(Duration.ofSeconds(10)))
        )
                .protocols(httpProtocol)
                .assertions(
                        global().responseTime().max().lt(600),
                        global().failedRequests().count().is(0L));
    }
}