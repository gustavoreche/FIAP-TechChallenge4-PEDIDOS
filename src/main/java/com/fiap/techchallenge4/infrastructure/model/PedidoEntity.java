package com.fiap.techchallenge4.infrastructure.model;

import com.fiap.techchallenge4.domain.StatusPedidoEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_pedido")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String cpfCliente;
    private Long ean;
    private Long quantidade;
    @Enumerated(EnumType.STRING)
    private StatusPedidoEnum statusPedido;
    private LocalDateTime dataDeCriacao;

}
