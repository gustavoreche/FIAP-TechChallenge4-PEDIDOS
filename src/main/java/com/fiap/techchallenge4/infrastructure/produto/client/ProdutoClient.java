package com.fiap.techchallenge4.infrastructure.produto.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "produto", url = "http://172.17.0.1:8080/produto")
public interface ProdutoClient {

    @GetMapping(value = "/estoque/{ean}/{quantidade}")
    Boolean temEstoque(@PathVariable(value = "ean") final Long ean,
                       @PathVariable(value = "quantidade") final Long quantidade);

}
