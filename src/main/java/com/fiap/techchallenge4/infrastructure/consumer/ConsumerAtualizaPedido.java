package com.fiap.techchallenge4.infrastructure.consumer;

import com.fiap.techchallenge4.infrastructure.consumer.response.AtualizaPedidoDTO;
import com.fiap.techchallenge4.useCase.PedidoUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
public class ConsumerAtualizaPedido {

    private final PedidoUseCase service;

    public ConsumerAtualizaPedido(final PedidoUseCase service) {
        this.service = service;
    }

    @Bean
    public Consumer<AtualizaPedidoDTO> atualiza() {
        return evento -> {
            this.service.atualiza(evento);
            System.out.println("Evento consumido com sucesso!");
        };
    }


}
