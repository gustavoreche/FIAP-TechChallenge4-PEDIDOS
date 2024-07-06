package com.fiap.techchallenge4.unitario;

import com.fiap.techchallenge4.domain.StatusAtualizaPedidoEnum;
import com.fiap.techchallenge4.domain.StatusPedidoEnum;
import com.fiap.techchallenge4.infrastructure.cliente.client.ClienteClient;
import com.fiap.techchallenge4.infrastructure.cliente.client.response.ClienteDTO;
import com.fiap.techchallenge4.infrastructure.consumer.response.AtualizaPedidoDTO;
import com.fiap.techchallenge4.infrastructure.controller.dto.CriaPedidoDTO;
import com.fiap.techchallenge4.infrastructure.model.PedidoEntity;
import com.fiap.techchallenge4.infrastructure.produto.client.ProdutoClient;
import com.fiap.techchallenge4.infrastructure.repository.PedidoRepository;
import com.fiap.techchallenge4.useCase.impl.PedidoUseCaseImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.cloud.stream.function.StreamBridge;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

public class PedidoUseCaseTest {

    @Test
    public void cria_salvaNaBaseDeDados() {
        // preparação
        var clientProduto = Mockito.mock(ProdutoClient.class);
        var clientCliente = Mockito.mock(ClienteClient.class);
        var streamBridge = Mockito.mock(StreamBridge.class);
        var repository = Mockito.mock(PedidoRepository.class);

        Mockito.when(repository.save(Mockito.any()))
                .thenReturn(
                        new PedidoEntity(
                                1L,
                                "71622958004",
                                7894900011517L,
                                100L,
                                StatusPedidoEnum.CRIADO,
                                LocalDateTime.now()
                        )
                );

        Mockito.when(clientCliente.pegaCliente(Mockito.any()))
                .thenReturn(
                        new ClienteDTO(
                                "71622958004",
                                "teste",
                                "teste",
                                100,
                                "SP",
                                LocalDateTime.now()
                        )
                );

        Mockito.when(clientProduto.temEstoque(Mockito.any(), Mockito.any()))
                .thenReturn(true);

        Mockito.when(streamBridge.send(Mockito.any(), Mockito.any()))
                .thenReturn(true);

        var service = new PedidoUseCaseImpl(clientProduto, clientCliente, streamBridge, repository);

        // execução
        boolean cria = service.cria(
                new CriaPedidoDTO(
                        7894900011517L,
                        "71622958004",
                        100L
                )
        );

        // avaliação
        verify(repository, times(1)).save(Mockito.any());
        verify(streamBridge, times(2)).send(Mockito.any(), Mockito.any());

        Assertions.assertTrue(cria);
    }

    @Test
    public void cria_naoSalvaNaBaseDeDados_clienteNaoEncontrado() {
        // preparação
        var clientProduto = Mockito.mock(ProdutoClient.class);
        var clientCliente = Mockito.mock(ClienteClient.class);
        var streamBridge = Mockito.mock(StreamBridge.class);
        var repository = Mockito.mock(PedidoRepository.class);

        Mockito.when(repository.save(Mockito.any()))
                .thenReturn(
                        new PedidoEntity(
                                1L,
                                "71622958004",
                                7894900011517L,
                                100L,
                                StatusPedidoEnum.CRIADO,
                                LocalDateTime.now()
                        )
                );

        Mockito.when(clientCliente.pegaCliente(Mockito.any()))
                .thenReturn(null);

        Mockito.when(clientProduto.temEstoque(Mockito.any(), Mockito.any()))
                .thenReturn(true);

        Mockito.when(streamBridge.send(Mockito.any(), Mockito.any()))
                .thenReturn(true);

        var service = new PedidoUseCaseImpl(clientProduto, clientCliente, streamBridge, repository);

        // execução
        boolean cria = service.cria(
                new CriaPedidoDTO(
                        7894900011517L,
                        "71622958004",
                        100L
                )
        );

        // avaliação
        verify(repository, times(0)).save(Mockito.any());
        verify(streamBridge, times(0)).send(Mockito.any(), Mockito.any());

        Assertions.assertFalse(cria);
    }

    @Test
    public void cria_naoSalvaNaBaseDeDados_semEstoque() {
        // preparação
        var clientProduto = Mockito.mock(ProdutoClient.class);
        var clientCliente = Mockito.mock(ClienteClient.class);
        var streamBridge = Mockito.mock(StreamBridge.class);
        var repository = Mockito.mock(PedidoRepository.class);

        Mockito.when(repository.save(Mockito.any()))
                .thenReturn(
                        new PedidoEntity(
                                1L,
                                "71622958004",
                                7894900011517L,
                                100L,
                                StatusPedidoEnum.CRIADO,
                                LocalDateTime.now()
                        )
                );

        Mockito.when(clientCliente.pegaCliente(Mockito.any()))
                .thenReturn(
                        new ClienteDTO(
                                "71622958004",
                                "teste",
                                "teste",
                                100,
                                "SP",
                                LocalDateTime.now()
                        )
                );

        Mockito.when(clientProduto.temEstoque(Mockito.any(), Mockito.any()))
                .thenReturn(false);

        Mockito.when(streamBridge.send(Mockito.any(), Mockito.any()))
                .thenReturn(true);

        var service = new PedidoUseCaseImpl(clientProduto, clientCliente, streamBridge, repository);

        // execução
        boolean cria = service.cria(
                new CriaPedidoDTO(
                        7894900011517L,
                        "71622958004",
                        100L
                )
        );

        // avaliação
        verify(repository, times(0)).save(Mockito.any());
        verify(streamBridge, times(0)).send(Mockito.any(), Mockito.any());

        Assertions.assertFalse(cria);
    }

    @Test
    public void cria_naoSalvaNaBaseDeDados_naoEncontrouProduto() {
        // preparação
        var clientProduto = Mockito.mock(ProdutoClient.class);
        var clientCliente = Mockito.mock(ClienteClient.class);
        var streamBridge = Mockito.mock(StreamBridge.class);
        var repository = Mockito.mock(PedidoRepository.class);

        Mockito.when(repository.save(Mockito.any()))
                .thenReturn(
                        new PedidoEntity(
                                1L,
                                "71622958004",
                                7894900011517L,
                                100L,
                                StatusPedidoEnum.CRIADO,
                                LocalDateTime.now()
                        )
                );

        Mockito.when(clientCliente.pegaCliente(Mockito.any()))
                .thenReturn(
                        new ClienteDTO(
                                "71622958004",
                                "teste",
                                "teste",
                                100,
                                "SP",
                                LocalDateTime.now()
                        )
                );

        Mockito.when(clientProduto.temEstoque(Mockito.any(), Mockito.any()))
                .thenReturn(null);

        Mockito.when(streamBridge.send(Mockito.any(), Mockito.any()))
                .thenReturn(true);

        var service = new PedidoUseCaseImpl(clientProduto, clientCliente, streamBridge, repository);

        // execução
        boolean cria = service.cria(
                new CriaPedidoDTO(
                        7894900011517L,
                        "71622958004",
                        100L
                )
        );

        // avaliação
        verify(repository, times(0)).save(Mockito.any());
        verify(streamBridge, times(0)).send(Mockito.any(), Mockito.any());

        Assertions.assertFalse(cria);
    }

    @Test
    public void cancela_salvaNaBaseDeDados() {
        // preparação
        var clientProduto = Mockito.mock(ProdutoClient.class);
        var clientCliente = Mockito.mock(ClienteClient.class);
        var streamBridge = Mockito.mock(StreamBridge.class);
        var repository = Mockito.mock(PedidoRepository.class);

        Mockito.when(repository.save(Mockito.any()))
                .thenReturn(
                        new PedidoEntity(
                                1L,
                                "71622958004",
                                7894900011517L,
                                100L,
                                StatusPedidoEnum.CRIADO,
                                LocalDateTime.now()
                        )
                );
        Mockito.when(repository.findByIdAndStatusPedido(Mockito.any(), Mockito.any()))
                .thenReturn(
                        Optional.of(
                                new PedidoEntity(
                                        1L,
                                        "71622958004",
                                        7894900011517L,
                                        100L,
                                        StatusPedidoEnum.CRIADO,
                                        LocalDateTime.now()
                                )
                        )
                );

        Mockito.when(streamBridge.send(Mockito.any(), Mockito.any()))
                .thenReturn(true);

        var service = new PedidoUseCaseImpl(clientProduto, clientCliente, streamBridge, repository);

        // execução
        boolean cria = service.cancela(1L);

        // avaliação
        verify(repository, times(1)).findByIdAndStatusPedido(Mockito.any(), Mockito.any());
        verify(repository, times(1)).save(Mockito.any());
        verify(streamBridge, times(2)).send(Mockito.any(), Mockito.any());

        Assertions.assertTrue(cria);
    }

    @Test
    public void cancela_naoSalvaNaBaseDeDados_statusPedidoDiferenteDeCriadoOuPedidoNaoEncontrado() {
        // preparação
        var clientProduto = Mockito.mock(ProdutoClient.class);
        var clientCliente = Mockito.mock(ClienteClient.class);
        var streamBridge = Mockito.mock(StreamBridge.class);
        var repository = Mockito.mock(PedidoRepository.class);

        Mockito.when(repository.save(Mockito.any()))
                .thenReturn(
                        new PedidoEntity(
                                1L,
                                "71622958004",
                                7894900011517L,
                                100L,
                                StatusPedidoEnum.EM_TRANSPORTE,
                                LocalDateTime.now()
                        )
                );
        Mockito.when(repository.findByIdAndStatusPedido(Mockito.any(), Mockito.any()))
                .thenReturn(
                        Optional.empty()
                );

        Mockito.when(streamBridge.send(Mockito.any(), Mockito.any()))
                .thenReturn(true);

        var service = new PedidoUseCaseImpl(clientProduto, clientCliente, streamBridge, repository);

        // execução
        boolean cria = service.cancela(1L);

        // avaliação
        verify(repository, times(1)).findByIdAndStatusPedido(Mockito.any(), Mockito.any());
        verify(repository, times(0)).save(Mockito.any());
        verify(streamBridge, times(0)).send(Mockito.any(), Mockito.any());

        Assertions.assertFalse(cria);
    }

    @Test
    public void atualiza_EMTRANSPORTE_salvaNaBaseDeDados() {
        // preparação
        var clientProduto = Mockito.mock(ProdutoClient.class);
        var clientCliente = Mockito.mock(ClienteClient.class);
        var streamBridge = Mockito.mock(StreamBridge.class);
        var repository = Mockito.mock(PedidoRepository.class);

        Mockito.when(repository.save(Mockito.any()))
                .thenReturn(
                        new PedidoEntity(
                                1L,
                                "71622958004",
                                7894900011517L,
                                100L,
                                StatusPedidoEnum.CRIADO,
                                LocalDateTime.now()
                        )
                );
        Mockito.when(repository.findByIdAndStatusPedido(Mockito.any(), Mockito.any()))
                .thenReturn(
                        Optional.of(
                                new PedidoEntity(
                                        1L,
                                        "71622958004",
                                        7894900011517L,
                                        100L,
                                        StatusPedidoEnum.CRIADO,
                                        LocalDateTime.now()
                                )
                        )
                );

        var service = new PedidoUseCaseImpl(clientProduto, clientCliente, streamBridge, repository);

        // execução
        service.atualiza(
                new AtualizaPedidoDTO(
                        1L,
                        StatusAtualizaPedidoEnum.EM_TRANSPORTE
                )
        );

        // avaliação
        verify(repository, times(1)).findByIdAndStatusPedido(Mockito.any(), Mockito.any());
        verify(repository, times(1)).save(Mockito.any());
        verifyNoInteractions(streamBridge);
    }

    @Test
    public void atualiza_EMTRANSPORTE_naoSalvaNaBaseDeDados_statusPedidoDiferenteDeCriadoOuPedidoNaoEncontrado() {
        // preparação
        var clientProduto = Mockito.mock(ProdutoClient.class);
        var clientCliente = Mockito.mock(ClienteClient.class);
        var streamBridge = Mockito.mock(StreamBridge.class);
        var repository = Mockito.mock(PedidoRepository.class);

        Mockito.when(repository.save(Mockito.any()))
                .thenReturn(
                        new PedidoEntity(
                                1L,
                                "71622958004",
                                7894900011517L,
                                100L,
                                StatusPedidoEnum.EM_TRANSPORTE,
                                LocalDateTime.now()
                        )
                );
        Mockito.when(repository.findByIdAndStatusPedido(Mockito.any(), Mockito.any()))
                .thenReturn(
                        Optional.empty()
                );

        var service = new PedidoUseCaseImpl(clientProduto, clientCliente, streamBridge, repository);

        // execução
        service.atualiza(
                new AtualizaPedidoDTO(
                        1L,
                        StatusAtualizaPedidoEnum.EM_TRANSPORTE
                )
        );

        // avaliação
        verify(repository, times(1)).findByIdAndStatusPedido(Mockito.any(), Mockito.any());
        verify(repository, times(0)).save(Mockito.any());
        verifyNoInteractions(streamBridge);
    }

    @Test
    public void atualiza_ENTREGUE_salvaNaBaseDeDados() {
        // preparação
        var clientProduto = Mockito.mock(ProdutoClient.class);
        var clientCliente = Mockito.mock(ClienteClient.class);
        var streamBridge = Mockito.mock(StreamBridge.class);
        var repository = Mockito.mock(PedidoRepository.class);

        Mockito.when(repository.save(Mockito.any()))
                .thenReturn(
                        new PedidoEntity(
                                1L,
                                "71622958004",
                                7894900011517L,
                                100L,
                                StatusPedidoEnum.CRIADO,
                                LocalDateTime.now()
                        )
                );
        Mockito.when(repository.findByIdAndStatusPedido(Mockito.any(), Mockito.any()))
                .thenReturn(
                        Optional.of(
                                new PedidoEntity(
                                        1L,
                                        "71622958004",
                                        7894900011517L,
                                        100L,
                                        StatusPedidoEnum.EM_TRANSPORTE,
                                        LocalDateTime.now()
                                )
                        )
                );

        var service = new PedidoUseCaseImpl(clientProduto, clientCliente, streamBridge, repository);

        // execução
        service.atualiza(
                new AtualizaPedidoDTO(
                        1L,
                        StatusAtualizaPedidoEnum.ENTREGUE
                )
        );

        // avaliação
        verify(repository, times(1)).findByIdAndStatusPedido(Mockito.any(), Mockito.any());
        verify(repository, times(1)).save(Mockito.any());
        verifyNoInteractions(streamBridge);
    }

    @Test
    public void atualiza_ENTREGUE_naoSalvaNaBaseDeDados_statusPedidoDiferenteDeEMTRANSPORTEOuPedidoNaoEncontrado() {
        // preparação
        var clientProduto = Mockito.mock(ProdutoClient.class);
        var clientCliente = Mockito.mock(ClienteClient.class);
        var streamBridge = Mockito.mock(StreamBridge.class);
        var repository = Mockito.mock(PedidoRepository.class);

        Mockito.when(repository.save(Mockito.any()))
                .thenReturn(
                        new PedidoEntity(
                                1L,
                                "71622958004",
                                7894900011517L,
                                100L,
                                StatusPedidoEnum.EM_TRANSPORTE,
                                LocalDateTime.now()
                        )
                );
        Mockito.when(repository.findByIdAndStatusPedido(Mockito.any(), Mockito.any()))
                .thenReturn(
                        Optional.empty()
                );

        var service = new PedidoUseCaseImpl(clientProduto, clientCliente, streamBridge, repository);

        // execução
        service.atualiza(
                new AtualizaPedidoDTO(
                        1L,
                        StatusAtualizaPedidoEnum.ENTREGUE
                )
        );

        // avaliação
        verify(repository, times(1)).findByIdAndStatusPedido(Mockito.any(), Mockito.any());
        verify(repository, times(0)).save(Mockito.any());
        verifyNoInteractions(streamBridge);
    }


    @ParameterizedTest
    @MethodSource("requestValidandoCampos")
    public void cria_camposInvalidos_naoSalvaNaBaseDeDados(Long ean,
                                                           String cpfCliente,
                                                           Long quantidade) {
        // preparação
        var clientProduto = Mockito.mock(ProdutoClient.class);
        var clientCliente = Mockito.mock(ClienteClient.class);
        var streamBridge = Mockito.mock(StreamBridge.class);
        var repository = Mockito.mock(PedidoRepository.class);

        Mockito.when(repository.save(Mockito.any()))
                .thenReturn(
                        new PedidoEntity(
                                1L,
                                "71622958004",
                                7894900011517L,
                                100L,
                                StatusPedidoEnum.CRIADO,
                                LocalDateTime.now()
                        )
                );

        Mockito.when(clientCliente.pegaCliente(Mockito.any()))
                .thenReturn(
                        new ClienteDTO(
                                "71622958004",
                                "teste",
                                "teste",
                                100,
                                "SP",
                                LocalDateTime.now()
                        )
                );

        Mockito.when(clientProduto.temEstoque(Mockito.any(), Mockito.any()))
                .thenReturn(true);

        Mockito.when(streamBridge.send(Mockito.any(), Mockito.any()))
                .thenReturn(true);

        var service = new PedidoUseCaseImpl(clientProduto, clientCliente, streamBridge, repository);

        // execução e avaliação
        var excecao = Assertions.assertThrows(RuntimeException.class, () -> {
            service.cria(
                    new CriaPedidoDTO(
                            ean,
                            cpfCliente,
                            quantidade
                    )
            );
        });
        verify(repository, times(0)).save(Mockito.any());
        verify(streamBridge, times(0)).send(Mockito.any(), Mockito.any());
    }

    @ParameterizedTest
    @ValueSource(longs = {
            -1000,
            -1L,
            0
    })
    public void cancela_camposInvalidos_naoBuscaNaBaseDeDados(Long idPedido) {
        // preparação
        var clientProduto = Mockito.mock(ProdutoClient.class);
        var clientCliente = Mockito.mock(ClienteClient.class);
        var streamBridge = Mockito.mock(StreamBridge.class);
        var repository = Mockito.mock(PedidoRepository.class);

        Mockito.when(repository.save(Mockito.any()))
                .thenReturn(
                        new PedidoEntity(
                                1L,
                                "71622958004",
                                7894900011517L,
                                100L,
                                StatusPedidoEnum.CRIADO,
                                LocalDateTime.now()
                        )
                );

        Mockito.when(streamBridge.send(Mockito.any(), Mockito.any()))
                .thenReturn(true);

        var service = new PedidoUseCaseImpl(clientProduto, clientCliente, streamBridge, repository);

        // execução e avaliação
        var excecao = Assertions.assertThrows(RuntimeException.class, () -> {
            service.cancela(
                    idPedido == -1000 ? null : idPedido
            );
        });
        verify(repository, times(0)).findByIdAndStatusPedido(Mockito.any(), Mockito.any());
        verify(repository, times(0)).save(Mockito.any());
        verify(streamBridge, times(0)).send(Mockito.any(), Mockito.any());
    }

    @ParameterizedTest
    @ValueSource(longs = {
            -1000,
            -1L,
            0
    })
    public void atualiza_EMTRANSPORTE_camposInvalidos_naoBuscaNaBaseDeDados(Long idPedido) {
        // preparação
        var clientProduto = Mockito.mock(ProdutoClient.class);
        var clientCliente = Mockito.mock(ClienteClient.class);
        var streamBridge = Mockito.mock(StreamBridge.class);
        var repository = Mockito.mock(PedidoRepository.class);

        Mockito.when(repository.save(Mockito.any()))
                .thenReturn(
                        new PedidoEntity(
                                1L,
                                "71622958004",
                                7894900011517L,
                                100L,
                                StatusPedidoEnum.CRIADO,
                                LocalDateTime.now()
                        )
                );

        var service = new PedidoUseCaseImpl(clientProduto, clientCliente, streamBridge, repository);

        // execução e avaliação
        var excecao = Assertions.assertThrows(RuntimeException.class, () -> {
            service.atualiza(
                    new AtualizaPedidoDTO(
                            idPedido == -1000 ? null : idPedido,
                            StatusAtualizaPedidoEnum.EM_TRANSPORTE
                    )
            );
        });
        verify(repository, times(0)).findByIdAndStatusPedido(Mockito.any(), Mockito.any());
        verify(repository, times(0)).save(Mockito.any());
        verifyNoInteractions(streamBridge);
    }

    @ParameterizedTest
    @ValueSource(longs = {
            -1000,
            -1L,
            0
    })
    public void atualiza_ENTREGUE_camposInvalidos_naoBuscaNaBaseDeDados(Long idPedido) {
        // preparação
        var clientProduto = Mockito.mock(ProdutoClient.class);
        var clientCliente = Mockito.mock(ClienteClient.class);
        var streamBridge = Mockito.mock(StreamBridge.class);
        var repository = Mockito.mock(PedidoRepository.class);

        Mockito.when(repository.save(Mockito.any()))
                .thenReturn(
                        new PedidoEntity(
                                1L,
                                "71622958004",
                                7894900011517L,
                                100L,
                                StatusPedidoEnum.CRIADO,
                                LocalDateTime.now()
                        )
                );

        var service = new PedidoUseCaseImpl(clientProduto, clientCliente, streamBridge, repository);

        // execução e avaliação
        var excecao = Assertions.assertThrows(RuntimeException.class, () -> {
            service.atualiza(
                    new AtualizaPedidoDTO(
                            idPedido == -1000 ? null : idPedido,
                            StatusAtualizaPedidoEnum.ENTREGUE
                    )
            );
        });
        verify(repository, times(0)).findByIdAndStatusPedido(Mockito.any(), Mockito.any());
        verify(repository, times(0)).save(Mockito.any());
        verifyNoInteractions(streamBridge);
    }


    private static Stream<Arguments> requestValidandoCampos() {
        return Stream.of(
                Arguments.of(null, "71622958004", 100L),
                Arguments.of(-1L, "71622958004", 100L),
                Arguments.of(0L, "71622958004", 100L),
                Arguments.of(123456789L, null, 100L),
                Arguments.of(123456789L, "", 100L),
                Arguments.of(123456789L, " ", 100L),
                Arguments.of(123456789L, "teste", 100L),
                Arguments.of(123456789L, "1234567891", 100L),
                Arguments.of(123456789L, "123456789123", 100L),
                Arguments.of(123456789L, "71622958004", null),
                Arguments.of(123456789L, "71622958004", -1L),
                Arguments.of(123456789L, "71622958004", 0L),
                Arguments.of(123456789L, "71622958004", 1001L)
        );
    }

}
