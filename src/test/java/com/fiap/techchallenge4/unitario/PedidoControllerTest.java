package com.fiap.techchallenge4.unitario;

import com.fiap.techchallenge4.infrastructure.controller.PedidoController;
import com.fiap.techchallenge4.infrastructure.controller.dto.CriaPedidoDTO;
import com.fiap.techchallenge4.useCase.impl.PedidoUseCaseImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

public class PedidoControllerTest {

    @Test
    public void cria_deveRetornar201_salvaNaBaseDeDados() {
        // preparação
        var service = Mockito.mock(PedidoUseCaseImpl.class);
        Mockito.when(service.cria(
                            any(CriaPedidoDTO.class)
                        )
                )
                .thenReturn(
                        true
                );

        var controller = new PedidoController(service);

        // execução
        var produto = controller.cria(
                new CriaPedidoDTO(
                        7894900011517L,
                        "71622958004",
                        100L
                )
        );

        // avaliação
        Assertions.assertEquals(HttpStatus.CREATED, produto.getStatusCode());
    }

    @Test
    public void cria_deveRetornar409_naoSalvaNaBaseDeDados() {
        // preparação
        var service = Mockito.mock(PedidoUseCaseImpl.class);
        Mockito.when(service.cria(
                                any(CriaPedidoDTO.class)
                        )
                )
                .thenReturn(
                        false
                );

        var controller = new PedidoController(service);

        // execução
        var produto = controller.cria(
                new CriaPedidoDTO(
                        7894900011517L,
                        "71622958004",
                        100L
                )
        );

        // avaliação
        Assertions.assertEquals(HttpStatus.CONFLICT, produto.getStatusCode());
    }

    @Test
    public void cancela_deveRetornar200_salvaNaBaseDeDados() {
        // preparação
        var service = Mockito.mock(PedidoUseCaseImpl.class);
        Mockito.when(service.cancela(
                                anyLong()
                        )
                )
                .thenReturn(
                        true
                );

        var controller = new PedidoController(service);

        // execução
        var produto = controller.cancela(1L);

        // avaliação
        Assertions.assertEquals(HttpStatus.OK, produto.getStatusCode());
    }

    @Test
    public void cancela_deveRetornar204_naoSalvaNaBaseDeDados() {
        // preparação
        var service = Mockito.mock(PedidoUseCaseImpl.class);
        Mockito.when(service.cancela(
                                anyLong()
                        )
                )
                .thenReturn(
                        false
                );

        var controller = new PedidoController(service);

        // execução
        var produto = controller.cancela(1L);

        // avaliação
        Assertions.assertEquals(HttpStatus.NO_CONTENT, produto.getStatusCode());
    }

    @Test
    public void atualizaParaEmTransporte_deveRetornar200_salvaNaBaseDeDados() {
        // preparação
        var service = Mockito.mock(PedidoUseCaseImpl.class);
        Mockito.when(service.atualizaParaEmTransporte(
                                anyLong()
                        )
                )
                .thenReturn(
                        true
                );

        var controller = new PedidoController(service);

        // execução
        var produto = controller.atualizaEmTransporte(1L);

        // avaliação
        Assertions.assertEquals(HttpStatus.OK, produto.getStatusCode());
    }

    @Test
    public void atualizaParaEmTransporte_deveRetornar204_naoSalvaNaBaseDeDados() {
        // preparação
        var service = Mockito.mock(PedidoUseCaseImpl.class);
        Mockito.when(service.atualizaParaEmTransporte(
                                anyLong()
                        )
                )
                .thenReturn(
                        false
                );

        var controller = new PedidoController(service);

        // execução
        var produto = controller.atualizaEmTransporte(1L);

        // avaliação
        Assertions.assertEquals(HttpStatus.NO_CONTENT, produto.getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("requestValidandoCampos")
    public void cria_camposInvalidos_naoSalvaNaBaseDeDados(Long ean,
                                                           String cpfCliente,
                                                           Long quantidade) {
        // preparação
        var service = Mockito.mock(PedidoUseCaseImpl.class);
        Mockito.doThrow(
                        new IllegalArgumentException("Campos inválidos!")
                )
                .when(service)
                .cria(
                        any(CriaPedidoDTO.class)
                );

        var controller = new PedidoController(service);

        // execução e avaliação
        var excecao = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            controller.cria(
                    new CriaPedidoDTO(
                            ean,
                            cpfCliente,
                            quantidade
                    )
            );
        });
    }

    @ParameterizedTest
    @ValueSource(longs = {
            -1000,
            -1L,
            0
    })
    public void cancela_camposInvalidos_naoSalvaNaBaseDeDados(Long idPedido) {
        // preparação
        var service = Mockito.mock(PedidoUseCaseImpl.class);
        Mockito.doThrow(
                        new IllegalArgumentException("Campos inválidos!")
                )
                .when(service)
                .cancela(
                        anyLong()
                );

        var controller = new PedidoController(service);

        // execução e avaliação
        var excecao = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            controller.cancela(idPedido);
        });
    }

    @ParameterizedTest
    @ValueSource(longs = {
            -1000,
            -1L,
            0
    })
    public void atualizaParaEmTransporte_camposInvalidos_naoSalvaNaBaseDeDados(Long idPedido) {
        // preparação
        var service = Mockito.mock(PedidoUseCaseImpl.class);
        Mockito.doThrow(
                        new IllegalArgumentException("Campos inválidos!")
                )
                .when(service)
                .atualizaParaEmTransporte(
                        anyLong()
                );

        var controller = new PedidoController(service);

        // execução e avaliação
        var excecao = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            controller.atualizaEmTransporte(idPedido);
        });
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
