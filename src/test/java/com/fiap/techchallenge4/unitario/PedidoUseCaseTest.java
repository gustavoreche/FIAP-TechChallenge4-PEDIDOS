package com.fiap.techchallenge4.unitario;

import com.fiap.techchallenge4.domain.StatusPedidoEnum;
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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class PedidoUseCaseTest {

    @Test
    public void cria_salvaNaBaseDeDados() {
        // preparação
        var client = Mockito.mock(ProdutoClient.class);
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

        Mockito.when(client.temEstoque(Mockito.any(), Mockito.any()))
                .thenReturn(true);

        Mockito.when(streamBridge.send(Mockito.any(), Mockito.any()))
                .thenReturn(true);

        var service = new PedidoUseCaseImpl(client, streamBridge, repository);

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
    public void cria_naoSalvaNaBaseDeDados_semEstoque() {
        // preparação
        var client = Mockito.mock(ProdutoClient.class);
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

        Mockito.when(client.temEstoque(Mockito.any(), Mockito.any()))
                .thenReturn(false);

        Mockito.when(streamBridge.send(Mockito.any(), Mockito.any()))
                .thenReturn(true);

        var service = new PedidoUseCaseImpl(client, streamBridge, repository);

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
        var client = Mockito.mock(ProdutoClient.class);
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

        Mockito.when(client.temEstoque(Mockito.any(), Mockito.any()))
                .thenReturn(null);

        Mockito.when(streamBridge.send(Mockito.any(), Mockito.any()))
                .thenReturn(true);

        var service = new PedidoUseCaseImpl(client, streamBridge, repository);

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
        var client = Mockito.mock(ProdutoClient.class);
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

        var service = new PedidoUseCaseImpl(client, streamBridge, repository);

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
        var client = Mockito.mock(ProdutoClient.class);
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

        var service = new PedidoUseCaseImpl(client, streamBridge, repository);

        // execução
        boolean cria = service.cancela(1L);

        // avaliação
        verify(repository, times(1)).findByIdAndStatusPedido(Mockito.any(), Mockito.any());
        verify(repository, times(0)).save(Mockito.any());
        verify(streamBridge, times(0)).send(Mockito.any(), Mockito.any());

        Assertions.assertFalse(cria);
    }

    @Test
    public void atualizaParaEmTransporte_salvaNaBaseDeDados() {
        // preparação
        var client = Mockito.mock(ProdutoClient.class);
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

        var service = new PedidoUseCaseImpl(client, streamBridge, repository);

        // execução
        boolean atualiza = service.atualizaParaEmTransporte(1L);

        // avaliação
        verify(repository, times(1)).findByIdAndStatusPedido(Mockito.any(), Mockito.any());
        verify(repository, times(1)).save(Mockito.any());
        verify(streamBridge, times(0)).send(Mockito.any(), Mockito.any());

        Assertions.assertTrue(atualiza);
    }

    @Test
    public void atualizaParaEmTransporte_naoSalvaNaBaseDeDados_statusPedidoDiferenteDeCriadoOuPedidoNaoEncontrado() {
        // preparação
        var client = Mockito.mock(ProdutoClient.class);
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

        var service = new PedidoUseCaseImpl(client, streamBridge, repository);

        // execução
        boolean atualiza = service.atualizaParaEmTransporte(1L);

        // avaliação
        verify(repository, times(1)).findByIdAndStatusPedido(Mockito.any(), Mockito.any());
        verify(repository, times(0)).save(Mockito.any());
        verify(streamBridge, times(0)).send(Mockito.any(), Mockito.any());

        Assertions.assertFalse(atualiza);
    }


    @ParameterizedTest
    @MethodSource("requestValidandoCampos")
    public void cria_camposInvalidos_naoSalvaNaBaseDeDados(Long ean,
                                                           String cpfCliente,
                                                           Long quantidade) {
        // preparação
        var client = Mockito.mock(ProdutoClient.class);
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

        Mockito.when(client.temEstoque(Mockito.any(), Mockito.any()))
                .thenReturn(true);

        Mockito.when(streamBridge.send(Mockito.any(), Mockito.any()))
                .thenReturn(true);

        var service = new PedidoUseCaseImpl(client, streamBridge, repository);

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
        var client = Mockito.mock(ProdutoClient.class);
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

        var service = new PedidoUseCaseImpl(client, streamBridge, repository);

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
    public void atualizaParaEmTransporte_camposInvalidos_naoBuscaNaBaseDeDados(Long idPedido) {
        // preparação
        var client = Mockito.mock(ProdutoClient.class);
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

        var service = new PedidoUseCaseImpl(client, streamBridge, repository);

        // execução e avaliação
        var excecao = Assertions.assertThrows(RuntimeException.class, () -> {
            service.atualizaParaEmTransporte(
                    idPedido == -1000 ? null : idPedido
            );
        });
        verify(repository, times(0)).findByIdAndStatusPedido(Mockito.any(), Mockito.any());
        verify(repository, times(0)).save(Mockito.any());
        verify(streamBridge, times(0)).send(Mockito.any(), Mockito.any());
    }

    private static Stream<Arguments> requestValidandoCampos() {
        return Stream.of(
                Arguments.of(null, "71622958004", 100L),
                Arguments.of(-1L, "71622958004", 100L),
                Arguments.of(0L, "71622958004", 100L),
                Arguments.of(0L, null, 100L),
                Arguments.of(0L, "", 100L),
                Arguments.of(0L, " ", 100L),
                Arguments.of(0L, "teste", 100L),
                Arguments.of(0L, "1234567891", 100L),
                Arguments.of(0L, "123456789123", 100L),
                Arguments.of(123456789L, "71622958004", null),
                Arguments.of(123456789L, "71622958004", -1L),
                Arguments.of(123456789L, "71622958004", 0L),
                Arguments.of(123456789L, "71622958004", 1001L)
        );
    }

}
