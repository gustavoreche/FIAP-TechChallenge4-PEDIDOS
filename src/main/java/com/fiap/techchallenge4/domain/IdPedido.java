package com.fiap.techchallenge4.domain;

import lombok.Getter;

import java.util.Objects;

@Getter
public class IdPedido {

    private Long numero;

    public IdPedido(final Long numero) {
        if (Objects.isNull(numero) || numero <= 0) {
            throw new IllegalArgumentException("EAN NAO PODE SER NULO OU MENOR E IGUAL A ZERO!");
        }

        this.numero = numero;
    }

}
