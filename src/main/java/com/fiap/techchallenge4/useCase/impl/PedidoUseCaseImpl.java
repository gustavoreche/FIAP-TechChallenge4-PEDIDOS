package com.fiap.techchallenge4.useCase.impl;

import com.fiap.techchallenge4.domain.Pedido;
import com.fiap.techchallenge4.domain.StatusPedidoEnum;
import com.fiap.techchallenge4.infrastructure.controller.dto.BaixaNoEstoqueDTO;
import com.fiap.techchallenge4.infrastructure.controller.dto.CriaPedidoDTO;
import com.fiap.techchallenge4.infrastructure.model.PedidoEntity;
import com.fiap.techchallenge4.infrastructure.produto.client.ProdutoClient;
import com.fiap.techchallenge4.infrastructure.repository.PedidoRepository;
import com.fiap.techchallenge4.useCase.PedidoUseCase;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class PedidoUseCaseImpl implements PedidoUseCase {

    private final ProdutoClient client;
    private final StreamBridge streamBridge;
    private final PedidoRepository repository;

    public PedidoUseCaseImpl(final ProdutoClient client,
                             final StreamBridge streamBridge,
                             final PedidoRepository repository) {
        this.client = client;
        this.streamBridge = streamBridge;
        this.repository = repository;
    }

    @Override
    public boolean cria(final CriaPedidoDTO dadosPedido) {
        final var pedido = new Pedido(
                dadosPedido.cpfCliente(),
                dadosPedido.ean(),
                dadosPedido.quantidade()
        );
        try {
            // TODO: falta verificar se o cliente existe

            //TODO: esse endpoint "temEstoque", pode devolver CODIGO 200(com resposta TRUE ou FALSE), CODIGO 204, E CODIGO 500
            final var temEstoque = this.client.temEstoque(pedido.getEan(), pedido.getQuantidade());
            if(Objects.nonNull(temEstoque) && temEstoque) {
                System.out.println("Pedido criado com sucesso");

                final var produtoEntity = PedidoEntity.builder()
                        .cpfCliente(pedido.getCpfCliente())
                        .ean(pedido.getEan())
                        .quantidade(pedido.getQuantidade())
                        .statusPedido(StatusPedidoEnum.CRIADO)
                        .dataDeCriacao(LocalDateTime.now())
                        .build();
                this.repository.save(produtoEntity);

                this.streamBridge.send("produto-atualiza-estoque", new BaixaNoEstoqueDTO(dadosPedido.ean(), dadosPedido.quantidade()));
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
