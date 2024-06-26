package com.fiap.techchallenge4.infrastructure.repository;

import com.fiap.techchallenge4.infrastructure.model.PedidoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<PedidoEntity, Long> {
}
