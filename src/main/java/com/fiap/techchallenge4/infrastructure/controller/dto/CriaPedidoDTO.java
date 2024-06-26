package com.fiap.techchallenge4.infrastructure.controller.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

public record CriaPedidoDTO(

		@JsonInclude(JsonInclude.Include.NON_NULL)
		Long ean,

		@JsonInclude(JsonInclude.Include.NON_NULL)
		String cpfCliente,

		@JsonInclude(JsonInclude.Include.NON_NULL)
		Long quantidade
) {}
