package com.fiap.techchallenge4.useCase;

import com.fiap.techchallenge4.infrastructure.controller.dto.CriaPedidoDTO;

public interface PedidoUseCase {

    boolean cria(final CriaPedidoDTO dadosPedido);

    boolean cancela(final Long idPedido);

    boolean atualizaParaEmTransporte(final Long idPedido);
}
