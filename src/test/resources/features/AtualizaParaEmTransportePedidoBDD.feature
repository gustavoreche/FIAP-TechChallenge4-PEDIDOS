# language: pt

Funcionalidade: Teste de atualizar o status do pedido para EM TRANSPORTE

  Cenário: Atualiza status para EM TRANSPORTE com sucesso
    Dado que informo um pedido que ja foi cadastrado
    Quando atualizo esse pedido para EM TRANSPORTE
    Entao recebo uma resposta que o pedido foi atualizado para EM TRANSPORTE com sucesso

  Cenário: Atualiza status para EM TRANSPORTE com status diferente de CRIADO
    Dado que informo um pedido que ja esta com o status EM TRANSPORTE
    Quando atualizo esse pedido para EM TRANSPORTE
    Entao recebo uma resposta que o pedido não foi atualizado para EM TRANSPORTE com sucesso

  Cenário: Atualiza status para EM TRANSPORTE um pedido que não existe
    Dado que informo um pedido que não existe
    Quando atualizo esse pedido para EM TRANSPORTE
    Entao recebo uma resposta que o pedido não foi atualizado para EM TRANSPORTE com sucesso
