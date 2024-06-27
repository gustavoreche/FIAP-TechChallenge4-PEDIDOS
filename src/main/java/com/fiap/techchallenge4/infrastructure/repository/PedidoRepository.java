package com.fiap.techchallenge4.infrastructure.repository;

import com.fiap.techchallenge4.domain.StatusPedidoEnum;
import com.fiap.techchallenge4.infrastructure.model.PedidoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PedidoRepository extends JpaRepository<PedidoEntity, Long> {

    Optional<PedidoEntity> findByIdAndStatusPedido(final Long id,
                                                   final StatusPedidoEnum statusPedido);

}
