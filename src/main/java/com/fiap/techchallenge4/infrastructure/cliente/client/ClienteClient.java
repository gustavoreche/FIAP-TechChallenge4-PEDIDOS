package com.fiap.techchallenge4.infrastructure.cliente.client;

import com.fiap.techchallenge4.infrastructure.cliente.client.response.ClienteDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "cliente", url = "http://172.17.0.1:8083/cliente")
public interface ClienteClient {

    @GetMapping(value = "/{cpf}")
    ClienteDTO pegaCliente(@PathVariable(value = "cpf") final String cpf);

}
