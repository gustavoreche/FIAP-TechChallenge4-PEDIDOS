package com.fiap.techchallenge4.infrastructure.controller.dto;

import com.fiap.techchallenge4.domain.StatusEstoqueEnum;

public record AtualizaEstoqueDTO(
		Long ean,
		Long quantidade,
		StatusEstoqueEnum statusEstoque
) {}
