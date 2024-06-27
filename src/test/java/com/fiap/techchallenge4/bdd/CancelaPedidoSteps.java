package com.fiap.techchallenge4.bdd;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import static com.fiap.techchallenge4.infrastructure.controller.PedidoController.URL_PEDIDOS_COM_ID;
import static io.restassured.RestAssured.given;


public class CancelaPedidoSteps {

    private Response response;
    private Long idPedido;

    @Dado("que informo um pedido que ja esta cadastrado")
    public void queInformoUmPedidoQueJaEstaCadastrado() {
        final var jdbcTemplate = this.criaConexaoComBaseDeDados();

        this.idPedido = System.currentTimeMillis();
        jdbcTemplate.execute("""
                INSERT INTO tb_pedido (id, cpf_cliente,data_de_criacao,ean,quantidade,status_pedido) VALUES
                	 (%s,'71622958004','2024-06-26 22:57:46.037',7894900011517,30,'CRIADO');
                """
                .formatted(this.idPedido));
    }

    @Dado("que informo um pedido que ja esta EM TRANSPORTE")
    public void queInformoUmPedidoQueJaEstaEmTransporte() {
        final var jdbcTemplate = criaConexaoComBaseDeDados();

        this.idPedido = System.currentTimeMillis();
        jdbcTemplate.execute("""
                INSERT INTO tb_pedido (id, cpf_cliente,data_de_criacao,ean,quantidade,status_pedido) VALUES
                	 (%s,'71622958004','2024-06-26 22:57:46.037',7894900011517,30,'EM_TRANSPORTE');
                """
                .formatted(this.idPedido));
    }

    @Dado("que informo um pedido que não foi criado")
    public void queInformoUmPedidoQueNaoFoiCriado() {
        this.idPedido = System.currentTimeMillis();
    }

    @Quando("cancelo esse pedido")
    public void canceloEssePedido() {
        RestAssured.baseURI = "http://localhost:8081";
        this.response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .delete(URL_PEDIDOS_COM_ID.replace("{idPedido}", this.idPedido.toString()));
    }

    @Entao("recebo uma resposta que o pedido foi cancelado com sucesso")
    public void receboUmaRespostaQueOPedidoFoiCanceladoComSucesso() {
        this.response
                .prettyPeek()
                .then()
                .statusCode(HttpStatus.OK.value())
        ;
    }

    @Entao("recebo uma resposta que o pedido não foi cancelado com sucesso")
    public void receboUmaRespostaQueOPedidoNaoFoiCanceladoComSucesso() {
        this.response
                .prettyPeek()
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value())
        ;
    }

    private JdbcTemplate criaConexaoComBaseDeDados() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl("jdbc:postgresql://localhost:5434/tech_challenge_4_pedidos");
        dataSource.setUsername("root");
        dataSource.setPassword("root");
        return new JdbcTemplate(dataSource);
    }

}
