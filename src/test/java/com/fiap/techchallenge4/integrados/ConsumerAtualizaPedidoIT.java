package com.fiap.techchallenge4.integrados;

import com.fiap.techchallenge4.domain.StatusAtualizaPedidoEnum;
import com.fiap.techchallenge4.domain.StatusPedidoEnum;
import com.fiap.techchallenge4.infrastructure.consumer.response.AtualizaPedidoDTO;
import com.fiap.techchallenge4.infrastructure.model.PedidoEntity;
import com.fiap.techchallenge4.infrastructure.repository.PedidoRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.function.StreamBridge;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ConsumerAtualizaPedidoIT {

    @Autowired
    private StreamBridge streamBridge;

    @Autowired
    PedidoRepository repository;

    @BeforeEach
    void inicializaLimpezaDoDatabase() {
        this.repository.deleteAll();
    }

    @AfterAll
    void finalizaLimpezaDoDatabase() {
        this.repository.deleteAll();
    }

    @Test
    public void atualiza_EMTRANSPORTE_salvaNaBaseDeDados() throws ExecutionException, InterruptedException {

        final var pedidoSalvo = this.repository.save(
                PedidoEntity.builder()
                        .cpfCliente("71622958004")
                        .ean(7894900011517L)
                        .quantidade(100L)
                        .statusPedido(StatusPedidoEnum.CRIADO)
                        .dataDeCriacao(LocalDateTime.now())
                        .build()
        );

        var producer = CompletableFuture.runAsync(() -> {
            this.streamBridge
                    .send("pedido-atualiza-status", new AtualizaPedidoDTO(
                            pedidoSalvo.getId(),
                            StatusAtualizaPedidoEnum.EM_TRANSPORTE
                            )
                    );
        });

        producer.get();
        Thread.sleep(2000);

        var entrega = this.repository.findAll().get(0);

        Assertions.assertEquals(1, this.repository.findAll().size());
        Assertions.assertEquals("71622958004", entrega.getCpfCliente());
        Assertions.assertEquals(7894900011517L, entrega.getEan());
        Assertions.assertEquals(100L, entrega.getQuantidade());
        Assertions.assertEquals(StatusPedidoEnum.EM_TRANSPORTE, entrega.getStatusPedido());
        Assertions.assertNotNull(entrega.getDataDeCriacao());
    }

    @Test
    public void atualiza_EMTRANSPORTE_naoSalvaNaBaseDeDados_statusPedidoDiferenteDeCriado() throws ExecutionException, InterruptedException {

        final var pedidoSalvo = this.repository.save(
                PedidoEntity.builder()
                        .cpfCliente("71622958004")
                        .ean(7894900011517L)
                        .quantidade(100L)
                        .statusPedido(StatusPedidoEnum.EM_TRANSPORTE)
                        .dataDeCriacao(LocalDateTime.now())
                        .build()
        );


        var producer = CompletableFuture.runAsync(() -> {
            this.streamBridge
                    .send("pedido-atualiza-status", new AtualizaPedidoDTO(
                            pedidoSalvo.getId(),
                                    StatusAtualizaPedidoEnum.EM_TRANSPORTE
                            )
                    );
        });

        producer.get();
        Thread.sleep(2000);

        var entrega = this.repository.findAll().get(0);

        Assertions.assertEquals(1, this.repository.findAll().size());
        Assertions.assertEquals("71622958004", entrega.getCpfCliente());
        Assertions.assertEquals(7894900011517L, entrega.getEan());
        Assertions.assertEquals(100L, entrega.getQuantidade());
        Assertions.assertEquals(StatusPedidoEnum.EM_TRANSPORTE, entrega.getStatusPedido());
        Assertions.assertNotNull(entrega.getDataDeCriacao());
    }

    @Test
    public void atualiza_EMTRANSPORTE_naoSalvaNaBaseDeDados_pedidoNaoEncontrado() throws ExecutionException, InterruptedException {

        var producer = CompletableFuture.runAsync(() -> {
            this.streamBridge
                    .send("pedido-atualiza-status", new AtualizaPedidoDTO(
                                    1L,
                                    StatusAtualizaPedidoEnum.EM_TRANSPORTE
                            )
                    );
        });

        producer.get();
        Thread.sleep(2000);

        Assertions.assertEquals(0, this.repository.findAll().size());
    }

    @Test
    public void atualiza_ENTREGUE_salvaNaBaseDeDados() throws ExecutionException, InterruptedException {

        final var pedidoSalvo = this.repository.save(
                PedidoEntity.builder()
                        .cpfCliente("71622958004")
                        .ean(7894900011517L)
                        .quantidade(100L)
                        .statusPedido(StatusPedidoEnum.EM_TRANSPORTE)
                        .dataDeCriacao(LocalDateTime.now())
                        .build()
        );

        var producer = CompletableFuture.runAsync(() -> {
            this.streamBridge
                    .send("pedido-atualiza-status", new AtualizaPedidoDTO(
                            pedidoSalvo.getId(),
                                    StatusAtualizaPedidoEnum.ENTREGUE
                            )
                    );
        });

        producer.get();
        Thread.sleep(2000);

        var entrega = this.repository.findAll().get(0);

        Assertions.assertEquals(1, this.repository.findAll().size());
        Assertions.assertEquals("71622958004", entrega.getCpfCliente());
        Assertions.assertEquals(7894900011517L, entrega.getEan());
        Assertions.assertEquals(100L, entrega.getQuantidade());
        Assertions.assertEquals(StatusPedidoEnum.ENTREGUE, entrega.getStatusPedido());
        Assertions.assertNotNull(entrega.getDataDeCriacao());
    }

    @Test
    public void atualiza_EMTREGUE_naoSalvaNaBaseDeDados_statusPedidoDiferenteDeEMTRANSPORTE() throws ExecutionException, InterruptedException {

        final var pedidoSalvo = this.repository.save(
                PedidoEntity.builder()
                        .cpfCliente("71622958004")
                        .ean(7894900011517L)
                        .quantidade(100L)
                        .statusPedido(StatusPedidoEnum.CANCELADO)
                        .dataDeCriacao(LocalDateTime.now())
                        .build()
        );

        var producer = CompletableFuture.runAsync(() -> {
            this.streamBridge
                    .send("pedido-atualiza-status", new AtualizaPedidoDTO(
                            pedidoSalvo.getId(),
                                    StatusAtualizaPedidoEnum.ENTREGUE
                            )
                    );
        });

        producer.get();
        Thread.sleep(2000);

        var entrega = this.repository.findAll().get(0);

        Assertions.assertEquals(1, this.repository.findAll().size());
        Assertions.assertEquals("71622958004", entrega.getCpfCliente());
        Assertions.assertEquals(7894900011517L, entrega.getEan());
        Assertions.assertEquals(100L, entrega.getQuantidade());
        Assertions.assertEquals(StatusPedidoEnum.CANCELADO, entrega.getStatusPedido());
        Assertions.assertNotNull(entrega.getDataDeCriacao());
    }

    @Test
    public void atualiza_ENTREGUE_naoSalvaNaBaseDeDados_pedidoNaoEncontrado() throws ExecutionException, InterruptedException {

        var producer = CompletableFuture.runAsync(() -> {
            this.streamBridge
                    .send("pedido-atualiza-status", new AtualizaPedidoDTO(
                                    1L,
                                    StatusAtualizaPedidoEnum.ENTREGUE
                            )
                    );
        });

        producer.get();
        Thread.sleep(2000);

        Assertions.assertEquals(0, this.repository.findAll().size());
    }

}
