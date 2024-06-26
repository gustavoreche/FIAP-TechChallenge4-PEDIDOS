package com.fiap.techchallenge4.infrastructure.controller;


import com.fiap.techchallenge4.infrastructure.controller.dto.CriaPedidoDTO;
import com.fiap.techchallenge4.useCase.PedidoUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.fiap.techchallenge4.infrastructure.controller.PedidoController.URL_PEDIDOS;

@Tag(
        name = "Pedidos",
        description = "Serviço para realizar o gerenciamento de pedidos no sistema"
)
@RestController
@RequestMapping(URL_PEDIDOS)
public class PedidoController {

    public static final String URL_PEDIDOS = "/pedido";

    private final PedidoUseCase service;

    public PedidoController(final PedidoUseCase service) {
        this.service = service;
    }

    @Operation(
            summary = "Serviço para criar um pedido"
    )
    @PostMapping
    public ResponseEntity<Void> cria(@RequestBody @Valid final CriaPedidoDTO dadosPedido) {
        final var criou = this.service.cria(dadosPedido);
        if(criou) {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .build();
        }
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .build();
    }

//    @Operation(
//            summary = "Serviço para cancelar um pedido"
//    )
//    @PostMapping
//    public ResponseEntity<Void> cancela(@RequestBody @Valid final CriaPedidoDTO dadosPedido) {
//        final var criou = this.service.cria(dadosPedido);
//        if(criou) {
//            return ResponseEntity
//                    .status(HttpStatus.CREATED)
//                    .build();
//        }
//        return ResponseEntity
//                .status(HttpStatus.CONFLICT)
//                .build();
//    }

//    @Operation(
//            summary = "Serviço para atualizar o status de um pedido"
//    )
//    @PostMapping
//    public ResponseEntity<Void> atualiza(@RequestBody @Valid final CriaPedidoDTO dadosPedido) {
//        final var criou = this.service.cria(dadosPedido);
//        if(criou) {
//            return ResponseEntity
//                    .status(HttpStatus.CREATED)
//                    .build();
//        }
//        return ResponseEntity
//                .status(HttpStatus.CONFLICT)
//                .build();
//    }

}
