package com.fiap.techchallenge4.bdd;

import com.fiap.techchallenge4.infrastructure.controller.dto.CriaPedidoDTO;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static com.fiap.techchallenge4.infrastructure.controller.PedidoController.URL_PEDIDOS;
import static io.restassured.RestAssured.given;


public class CriaPedidoSteps {

    private Response response;
    private CriaPedidoDTO request;
    private Long ean;
    private ClientAndServer mockServerProduto;
    private ClientAndServer mockServerCliente;

    @Dado("que informo os dados do pedido")
    public void queInformoOsDadosDoPedido() {
        this.ean = System.currentTimeMillis();
        this.request = new CriaPedidoDTO(
                this.ean,
                "71622958004",
                1L
        );

        this.mockServerProduto = this.criaMockServerProduto();
        this.mockServerCliente = this.criaMockServerCliente();
    }

    @Dado("que informo um cliente não cadastrado")
    public void queInformoUmClienteNaoCadastrado() {
        this.ean = System.currentTimeMillis();
        this.request = new CriaPedidoDTO(
                this.ean,
                "12345678901",
                2L
        );

        this.mockServerProduto = this.criaMockServerProduto();
        this.mockServerCliente = this.criaMockServerCliente();
    }


    @Dado("que informo um produto sem estoque")
    public void queInformoUmProdutoSemEstoque() {
        this.ean = System.currentTimeMillis();
        this.request = new CriaPedidoDTO(
                this.ean,
                "71622958004",
                2L
        );

        this.mockServerProduto = this.criaMockServerProduto();
        this.mockServerCliente = this.criaMockServerCliente();
    }

    @Dado("que informo um produto que não existe")
    public void queInformoUmProdutoQueNaoExiste() {
        this.ean = System.currentTimeMillis();
        this.request = new CriaPedidoDTO(
                this.ean,
                "71622958004",
                3L
        );

        this.mockServerProduto = this.criaMockServerProduto();
        this.mockServerCliente = this.criaMockServerCliente();
    }

    @Quando("crio esse pedido")
    public void crioEssePedido() {
        RestAssured.baseURI = "http://localhost:8081";
        this.response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(this.request)
                .when()
                .post(URL_PEDIDOS);
    }

    @Entao("recebo uma resposta que o pedido foi criado com sucesso")
    public void receboUmaRespostaQueOPedidoFoiCriadoComSucesso() {
        this.response
                .prettyPeek()
                .then()
                .statusCode(HttpStatus.CREATED.value())
        ;

        this.mockServerProduto.stop();
        this.mockServerCliente.stop();
    }

    @Entao("recebo uma resposta que o pedido não foi criado")
    public void receboUmaRespostaQueOPedidoNaoFoiCriado() {
        this.response
                .prettyPeek()
                .then()
                .statusCode(HttpStatus.CONFLICT.value())
        ;

        this.mockServerProduto.stop();
        this.mockServerCliente.stop();
    }

    private ClientAndServer criaMockServerProduto() {
        final var clientAndServer = ClientAndServer.startClientAndServer(8080);

        clientAndServer.when(
                        HttpRequest.request()
                                .withMethod("GET")
                                .withPath("/produto/estoque/{ean}/1".replace("{ean}", this.ean.toString()))
                )
                .respond(
                        HttpResponse.response()
                                .withContentType(org.mockserver.model.MediaType.APPLICATION_JSON)
                                .withStatusCode(200)
                                .withBody("true")
                );

        clientAndServer.when(
                        HttpRequest.request()
                                .withMethod("GET")
                                .withPath("/produto/estoque/{ean}/2".replace("{ean}", this.ean.toString()))
                )
                .respond(
                        HttpResponse.response()
                                .withContentType(org.mockserver.model.MediaType.APPLICATION_JSON)
                                .withStatusCode(200)
                                .withBody("false")
                );

        clientAndServer.when(
                        HttpRequest.request()
                                .withMethod("GET")
                                .withPath("/produto/estoque/{ean}/3".replace("{ean}", this.ean.toString()))
                )
                .respond(
                        HttpResponse.response()
                                .withContentType(org.mockserver.model.MediaType.APPLICATION_JSON)
                                .withStatusCode(204)
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
                                .withContentType(org.mockserver.model.MediaType.APPLICATION_JSON)
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

        clientAndServer.when(
                        HttpRequest.request()
                                .withMethod("GET")
                                .withPath("/cliente/12345678901")
                )
                .respond(
                        HttpResponse.response()
                                .withContentType(org.mockserver.model.MediaType.APPLICATION_JSON)
                                .withStatusCode(204)
                );

        return clientAndServer;
    }


}
