package com.fiap.techchallenge4.useCase.impl;

import com.fiap.techchallenge4.domain.IdPedido;
import com.fiap.techchallenge4.domain.Pedido;
import com.fiap.techchallenge4.domain.StatusEstoqueEnum;
import com.fiap.techchallenge4.domain.StatusPedidoEnum;
import com.fiap.techchallenge4.infrastructure.cliente.client.ClienteClient;
import com.fiap.techchallenge4.infrastructure.controller.dto.AtualizaEstoqueDTO;
import com.fiap.techchallenge4.infrastructure.controller.dto.CriaPedidoDTO;
import com.fiap.techchallenge4.infrastructure.controller.dto.PreparaEntregaDTO;
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

    private final ProdutoClient clientProduto;
    private final ClienteClient clientCliente;
    private final StreamBridge streamBridge;
    private final PedidoRepository repository;

    public PedidoUseCaseImpl(final ProdutoClient clientProduto,
                             final ClienteClient clientCliente,
                             final StreamBridge streamBridge,
                             final PedidoRepository repository) {
        this.clientProduto = clientProduto;
        this.clientCliente = clientCliente;
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
            final var cliente = this.clientCliente.pegaCliente(pedido.getCpfCliente());

            final var temEstoque = this.clientProduto.temEstoque(pedido.getEan(), pedido.getQuantidade());
            if(Objects.nonNull(cliente) && Objects.nonNull(temEstoque) && temEstoque) {
                System.out.println("Pedido criado com sucesso");

                final var produtoEntity = PedidoEntity.builder()
                        .cpfCliente(pedido.getCpfCliente())
                        .ean(pedido.getEan())
                        .quantidade(pedido.getQuantidade())
                        .statusPedido(StatusPedidoEnum.CRIADO)
                        .dataDeCriacao(LocalDateTime.now())
                        .build();
                final var pedidoSalvoNaBase = this.repository.save(produtoEntity);

                this.streamBridge.send("produto-atualiza-estoque", new AtualizaEstoqueDTO(
                        dadosPedido.ean(),
                        dadosPedido.quantidade(),
                        StatusEstoqueEnum.RETIRA_DO_ESTOQUE
                        )
                );

                this.streamBridge.send("logistica-prepara-entrega", new PreparaEntregaDTO(
                        pedidoSalvoNaBase.getId(),
                        dadosPedido.cpfCliente(),
                        dadosPedido.ean(),
                        dadosPedido.quantidade()
                        )
                );
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean cancela(final Long idPedido) {
        final var idPedidoObjeto = new IdPedido(idPedido);

        final var pedidoNaBase = this.repository.findByIdAndStatusPedido(idPedidoObjeto.getNumero(), StatusPedidoEnum.CRIADO);
        if(pedidoNaBase.isEmpty()) {
            System.out.println("Pedido não está cadastrado");
            return false;
        }
        final var pedido = pedidoNaBase.get();

        final var produtoEntity = PedidoEntity.builder()
                .id(idPedidoObjeto.getNumero())
                .cpfCliente(pedido.getCpfCliente())
                .ean(pedido.getEan())
                .quantidade(pedido.getQuantidade())
                .statusPedido(StatusPedidoEnum.CANCELADO)
                .dataDeCriacao(LocalDateTime.now())
                .build();
        this.repository.save(produtoEntity);
        this.streamBridge.send("produto-atualiza-estoque", new AtualizaEstoqueDTO(
                pedido.getEan(),
                pedido.getQuantidade(),
                StatusEstoqueEnum.VOLTA_PARA_O_ESTOQUE));

        this.streamBridge.send("logistica-prepara-entrega", new PreparaEntregaDTO(
                idPedido,
                produtoEntity.getCpfCliente(),
                produtoEntity.getEan(),
                produtoEntity.getQuantidade()
                )
        );
        return true;

    }

    @Override
    public boolean atualizaParaEmTransporte(final Long idPedido) {
        final var idPedidoObjeto = new IdPedido(idPedido);

        final var pedidoNaBase = this.repository.findByIdAndStatusPedido(idPedidoObjeto.getNumero(), StatusPedidoEnum.CRIADO);
        if(pedidoNaBase.isEmpty()) {
            System.out.println("Pedido não está cadastrado ou está com outros STATUS");
            return false;
        }
        final var pedido = pedidoNaBase.get();

        final var produtoEntity = PedidoEntity.builder()
                .id(idPedidoObjeto.getNumero())
                .cpfCliente(pedido.getCpfCliente())
                .ean(pedido.getEan())
                .quantidade(pedido.getQuantidade())
                .statusPedido(StatusPedidoEnum.EM_TRANSPORTE)
                .dataDeCriacao(LocalDateTime.now())
                .build();
        this.repository.save(produtoEntity);
        return true;
    }

}
