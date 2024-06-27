# language: pt

Funcionalidade: Teste de criar o pedido

  Cenário: Cria pedido com sucesso
    Dado que informo os dados do pedido
    Quando crio esse pedido
    Entao recebo uma resposta que o pedido foi criado com sucesso

  Cenário: Cria pedido com produto sem estoque
    Dado que informo um produto sem estoque
    Quando crio esse pedido
    Entao recebo uma resposta que o pedido não foi criado

  Cenário: Cria pedido com produto que não existe
    Dado que informo um produto que não existe
    Quando crio esse pedido
    Entao recebo uma resposta que o pedido não foi criado
