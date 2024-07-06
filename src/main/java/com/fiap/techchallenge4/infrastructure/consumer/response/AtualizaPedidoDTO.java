package com.fiap.techchallenge4.infrastructure.consumer.response;

import com.fiap.techchallenge4.domain.StatusAtualizaPedidoEnum;

public record AtualizaPedidoDTO(
		Long idDoPedido,
		StatusAtualizaPedidoEnum statusEntrega
) {}
