# language: pt

Funcionalidade: Teste de cancelar o pedido

  Cenário: Cancela pedido com sucesso
    Dado que informo um pedido que ja esta cadastrado
    Quando cancelo esse pedido
    Entao recebo uma resposta que o pedido foi cancelado com sucesso

  Cenário: Cancela pedido com status diferente de CRIADO
    Dado que informo um pedido que ja esta EM TRANSPORTE
    Quando cancelo esse pedido
    Entao recebo uma resposta que o pedido não foi cancelado com sucesso

  Cenário: Cancela pedido que não existe
    Dado que informo um pedido que não foi criado
    Quando cancelo esse pedido
    Entao recebo uma resposta que o pedido não foi cancelado com sucesso
