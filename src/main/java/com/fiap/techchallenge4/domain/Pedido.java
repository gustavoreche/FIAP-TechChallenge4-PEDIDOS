package com.fiap.techchallenge4.domain;

import lombok.Getter;

import java.util.Objects;

@Getter
public class Pedido {
    private String cpfCliente;
    private Long ean;
    private Long quantidade;

    public static final String REGEX_CPF = "(^\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}$)";

    public Pedido(final String cpfCliente,
                  final Long ean,
                  final Long quantidade) {
        if (Objects.isNull(cpfCliente) || cpfCliente.isEmpty()) {
            throw new IllegalArgumentException("CPF NAO PODE SER NULO OU VAZIO!");
        }
        if (!cpfCliente.matches(REGEX_CPF)) {
            throw new IllegalArgumentException("CPF DO CLIENTE INVÁLIDO!");
        }

        if (Objects.isNull(ean) || ean <= 0) {
            throw new IllegalArgumentException("EAN NAO PODE SER NULO OU MENOR E IGUAL A ZERO!");
        }

        if (Objects.isNull(quantidade) || (quantidade <= 0 || quantidade > 1000)) {
            throw new IllegalArgumentException("QUANTIDADE NAO PODE SER NULO OU MENOR E IGUAL A ZERO E MAIOR QUE 1000!");
        }

        this.cpfCliente = cpfCliente;
        this.ean = ean;
        this.quantidade = quantidade;
    }

}
