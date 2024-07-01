package com.fiap.techchallenge4.infrastructure.controller.dto;

public record PreparaEntregaDTO(
		Long idDoPedido,
		String cpfCliente,
		Long ean,
		Long quantidade
) {}
