package com.fiap.techchallenge4.infrastructure.controller.dto;

public record PreparaEntregaDTO(
		String cpfCliente,
		Long ean,
		Long quantidade
) {}
