package com.fiap.techchallenge4.useCase;

import com.fiap.techchallenge4.infrastructure.consumer.response.AtualizaPedidoDTO;
import com.fiap.techchallenge4.infrastructure.controller.dto.CriaPedidoDTO;

public interface PedidoUseCase {

    boolean cria(final CriaPedidoDTO dadosPedido);

    boolean cancela(final Long idPedido);

    void atualiza(final AtualizaPedidoDTO evento);
}
