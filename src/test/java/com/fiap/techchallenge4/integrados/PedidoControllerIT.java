package com.fiap.techchallenge4.integrados;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.techchallenge4.domain.StatusEstoqueEnum;
import com.fiap.techchallenge4.domain.StatusPedidoEnum;
import com.fiap.techchallenge4.infrastructure.cliente.client.ClienteClient;
import com.fiap.techchallenge4.infrastructure.cliente.client.response.ClienteDTO;
import com.fiap.techchallenge4.infrastructure.controller.dto.AtualizaEstoqueDTO;
import com.fiap.techchallenge4.infrastructure.controller.dto.CriaPedidoDTO;
import com.fiap.techchallenge4.infrastructure.controller.dto.PreparaEntregaDTO;
import com.fiap.techchallenge4.infrastructure.model.PedidoEntity;
import com.fiap.techchallenge4.infrastructure.produto.client.ProdutoClient;
import com.fiap.techchallenge4.infrastructure.repository.PedidoRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static com.fiap.techchallenge4.infrastructure.controller.PedidoController.URL_PEDIDOS;
import static com.fiap.techchallenge4.infrastructure.controller.PedidoController.URL_PEDIDOS_COM_ID;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@AutoConfigureMockMvc
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PedidoControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    PedidoRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @MockBean
    StreamBridge streamBridge;

    @Autowired
    @MockBean
    ProdutoClient clientProduto;

    @Autowired
    @MockBean
    ClienteClient clientCliente;

    @BeforeEach
    void inicializaLimpezaDoDatabase() {
        this.repository.deleteAll();
    }

    @AfterAll
    void finalizaLimpezaDoDatabase() {
        this.repository.deleteAll();
    }

    @Test
    public void cria_deveRetornar201_salvaNaBaseDeDados() throws Exception {
        Mockito.when(this.clientCliente.pegaCliente("71622958004"))
                .thenReturn(
                        new ClienteDTO(
                                "71622958004",
                                "teste",
                                "teste",
                                100,
                                "SP",
                                LocalDateTime.now()
                        )
                );
        Mockito.when(this.clientProduto.temEstoque(7894900011517L, 3L))
                .thenReturn(
                        true
                );
        Mockito.when(this.streamBridge.send("produto-atualiza-estoque", new AtualizaEstoqueDTO(
                7894900011517L,
                        3L,
                        StatusEstoqueEnum.RETIRA_DO_ESTOQUE)))
                .thenReturn(
                        true
                );
        Mockito.when(this.streamBridge.send("logistica-prepara-entrega", new PreparaEntregaDTO(
                        1L,
                        "71622958004",
                        7894900011517L,
                        1L)))
                .thenReturn(
                        true
                );

        var request = new CriaPedidoDTO(
                7894900011517L,
                "71622958004",
                3L
        );
        var objectMapper = this.objectMapper
                .writer()
                .withDefaultPrettyPrinter();
        var jsonRequest = objectMapper.writeValueAsString(request);

        this.mockMvc
                .perform(MockMvcRequestBuilders.post(URL_PEDIDOS)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .status()
                        .isCreated()
                )
                .andReturn();

        var produto = this.repository.findAll().get(0);

        Assertions.assertNotNull(produto.getId());
        Assertions.assertEquals(7894900011517L, produto.getEan());
        Assertions.assertEquals("71622958004", produto.getCpfCliente());
        Assertions.assertEquals(3L, produto.getQuantidade());
        Assertions.assertEquals(StatusPedidoEnum.CRIADO, produto.getStatusPedido());
        Assertions.assertNotNull(produto.getDataDeCriacao());
        verify(streamBridge, times(2)).send(Mockito.any(), Mockito.any());
    }

    @Test
    public void cria_deveRetornar409_naoSalvaNaBaseDeDados_clienteNaoEncontrado() throws Exception {
        Mockito.when(this.clientCliente.pegaCliente("71622958004"))
                .thenReturn(null);
        Mockito.when(this.clientProduto.temEstoque(7894900011517L, 3L))
                .thenReturn(
                        true
                );
        Mockito.when(this.streamBridge.send("produto-atualiza-estoque", new AtualizaEstoqueDTO(
                        7894900011517L,
                        3L,
                        StatusEstoqueEnum.RETIRA_DO_ESTOQUE)))
                .thenReturn(
                        true
                );
        Mockito.when(this.streamBridge.send("logistica-prepara-entrega", new PreparaEntregaDTO(
                        1L,
                        "71622958004",
                        7894900011517L,
                        1L)))
                .thenReturn(
                        true
                );

        var request = new CriaPedidoDTO(
                7894900011517L,
                "71622958004",
                3L
        );
        var objectMapper = this.objectMapper
                .writer()
                .withDefaultPrettyPrinter();
        var jsonRequest = objectMapper.writeValueAsString(request);

        this.mockMvc
                .perform(MockMvcRequestBuilders.post(URL_PEDIDOS)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .status()
                        .isConflict()
                )
                .andReturn();

        Assertions.assertEquals(0, this.repository.findAll().size());
        verify(streamBridge, times(0)).send(Mockito.any(), Mockito.any());
    }

    @Test
    public void cria_deveRetornar409_naoSalvaNaBaseDeDados_semEstoque() throws Exception {
        Mockito.when(this.clientCliente.pegaCliente("71622958004"))
                .thenReturn(
                        new ClienteDTO(
                                "71622958004",
                                "teste",
                                "teste",
                                100,
                                "SP",
                                LocalDateTime.now()
                        )
                );
        Mockito.when(this.clientProduto.temEstoque(7894900011517L, 3L))
                .thenReturn(
                        false
                );
        Mockito.when(this.streamBridge.send("produto-atualiza-estoque", new AtualizaEstoqueDTO(
                7894900011517L,
                        3L,
                        StatusEstoqueEnum.RETIRA_DO_ESTOQUE)))
                .thenReturn(
                        true
                );
        Mockito.when(this.streamBridge.send("logistica-prepara-entrega", new PreparaEntregaDTO(
                        1L,
                        "71622958004",
                        7894900011517L,
                        1L)))
                .thenReturn(
                        true
                );

        var request = new CriaPedidoDTO(
                7894900011517L,
                "71622958004",
                3L
        );
        var objectMapper = this.objectMapper
                .writer()
                .withDefaultPrettyPrinter();
        var jsonRequest = objectMapper.writeValueAsString(request);

        this.mockMvc
                .perform(MockMvcRequestBuilders.post(URL_PEDIDOS)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .status()
                        .isConflict()
                )
                .andReturn();

        Assertions.assertEquals(0, this.repository.findAll().size());
        verify(streamBridge, times(0)).send(Mockito.any(), Mockito.any());
    }

    @Test
    public void cria_deveRetornar409_naoSalvaNaBaseDeDados_apiClienteIndisponivel() throws Exception {
        Mockito.doThrow(
                        new RuntimeException("API INDISPONIVEL!!")
                )
                .when(this.clientCliente)
                .pegaCliente("71622958004");
        Mockito.when(this.clientProduto.temEstoque(7894900011517L, 3L))
                .thenReturn(
                        true
                );
        Mockito.when(this.streamBridge.send("produto-atualiza-estoque", new AtualizaEstoqueDTO(
                        7894900011517L,
                        3L,
                        StatusEstoqueEnum.RETIRA_DO_ESTOQUE)))
                .thenReturn(
                        true
                );
        Mockito.when(this.streamBridge.send("logistica-prepara-entrega", new PreparaEntregaDTO(
                        1L,
                        "71622958004",
                        7894900011517L,
                        1L)))
                .thenReturn(
                        true
                );

        var request = new CriaPedidoDTO(
                7894900011517L,
                "71622958004",
                3L
        );
        var objectMapper = this.objectMapper
                .writer()
                .withDefaultPrettyPrinter();
        var jsonRequest = objectMapper.writeValueAsString(request);

        this.mockMvc
                .perform(MockMvcRequestBuilders.post(URL_PEDIDOS)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .status()
                        .isConflict()
                )
                .andReturn();

        Assertions.assertEquals(0, this.repository.findAll().size());
        verify(streamBridge, times(0)).send(Mockito.any(), Mockito.any());
    }

    @Test
    public void cria_deveRetornar409_naoSalvaNaBaseDeDados_apiProdutoIndisponivel() throws Exception {
        Mockito.when(this.clientCliente.pegaCliente("71622958004"))
                .thenReturn(
                        new ClienteDTO(
                                "71622958004",
                                "teste",
                                "teste",
                                100,
                                "SP",
                                LocalDateTime.now()
                        )
                );
        Mockito.doThrow(
                        new RuntimeException("API INDISPONIVEL!!")
                )
                .when(this.clientProduto)
                .temEstoque(7894900011517L, 3L);
        Mockito.when(this.streamBridge.send("produto-atualiza-estoque", new AtualizaEstoqueDTO(
                7894900011517L,
                        3L,
                        StatusEstoqueEnum.RETIRA_DO_ESTOQUE)))
                .thenReturn(
                        true
                );
        Mockito.when(this.streamBridge.send("logistica-prepara-entrega", new PreparaEntregaDTO(
                        1L,
                        "71622958004",
                        7894900011517L,
                        1L)))
                .thenReturn(
                        true
                );

        var request = new CriaPedidoDTO(
                7894900011517L,
                "71622958004",
                3L
        );
        var objectMapper = this.objectMapper
                .writer()
                .withDefaultPrettyPrinter();
        var jsonRequest = objectMapper.writeValueAsString(request);

        this.mockMvc
                .perform(MockMvcRequestBuilders.post(URL_PEDIDOS)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .status()
                        .isConflict()
                )
                .andReturn();

        Assertions.assertEquals(0, this.repository.findAll().size());
        verify(streamBridge, times(0)).send(Mockito.any(), Mockito.any());
    }

    @Test
    public void cria_deveRetornar409_naoSalvaNaBaseDeDados_naoEncontrouProduto() throws Exception {
        Mockito.when(this.clientCliente.pegaCliente("71622958004"))
                .thenReturn(
                        new ClienteDTO(
                                "71622958004",
                                "teste",
                                "teste",
                                100,
                                "SP",
                                LocalDateTime.now()
                        )
                );
        Mockito.when(this.clientProduto.temEstoque(7894900011517L, 3L))
                .thenReturn(
                        null
                );
        Mockito.when(this.streamBridge.send("produto-atualiza-estoque", new AtualizaEstoqueDTO(
                7894900011517L,
                        3L,
                        StatusEstoqueEnum.RETIRA_DO_ESTOQUE)))
                .thenReturn(
                        true
                );
        Mockito.when(this.streamBridge.send("logistica-prepara-entrega", new PreparaEntregaDTO(
                        1L,
                        "71622958004",
                        7894900011517L,
                        1L)))
                .thenReturn(
                        true
                );

        var request = new CriaPedidoDTO(
                7894900011517L,
                "71622958004",
                3L
        );
        var objectMapper = this.objectMapper
                .writer()
                .withDefaultPrettyPrinter();
        var jsonRequest = objectMapper.writeValueAsString(request);

        this.mockMvc
                .perform(MockMvcRequestBuilders.post(URL_PEDIDOS)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .status()
                        .isConflict()
                )
                .andReturn();

        Assertions.assertEquals(0, this.repository.findAll().size());
        verify(streamBridge, times(0)).send(Mockito.any(), Mockito.any());
    }

    @Test
    public void cancela_deveRetornar200_salvaNaBaseDeDados() throws Exception {
        Mockito.when(this.streamBridge.send("produto-volta-estoque", new AtualizaEstoqueDTO(
                7894900011517L,
                        3L,
                        StatusEstoqueEnum.VOLTA_PARA_O_ESTOQUE)))
                .thenReturn(
                        true
                );
        Mockito.when(this.streamBridge.send("logistica-prepara-entrega", new PreparaEntregaDTO(
                        1L,
                        "71622958004",
                        7894900011517L,
                       1L)))
                .thenReturn(
                        true
                );

        final var pedido = PedidoEntity.builder()
                .cpfCliente("71622958004")
                .ean(7894900011517L)
                .quantidade(30L)
                .statusPedido(StatusPedidoEnum.CRIADO)
                .dataDeCriacao(LocalDateTime.now())
                .build();
        final var pedidoSalvo = this.repository.save(pedido);

        this.mockMvc
                .perform(MockMvcRequestBuilders.delete(URL_PEDIDOS_COM_ID.replace("{idPedido}", pedidoSalvo.getId().toString()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .status()
                        .isOk()
                )
                .andReturn();

        final var pedidoEntity = this.repository.findAll().get(0);

        Assertions.assertEquals(1, this.repository.findAll().size());
        Assertions.assertEquals(pedidoSalvo.getId(), pedidoEntity.getId());
        Assertions.assertEquals("71622958004", pedidoEntity.getCpfCliente());
        Assertions.assertEquals(7894900011517L, pedidoEntity.getEan());
        Assertions.assertEquals(30L, pedidoEntity.getQuantidade());
        Assertions.assertEquals(StatusPedidoEnum.CANCELADO, pedidoEntity.getStatusPedido());
        Assertions.assertNotNull(pedidoEntity.getDataDeCriacao());
        verify(streamBridge, times(2)).send(Mockito.any(), Mockito.any());
    }

    @Test
    public void cancela_deveRetornar204_naoSalvaNaBaseDeDados_statusPedidoDiferenteDeCriado() throws Exception {
        Mockito.when(this.streamBridge.send("produto-volta-estoque", new AtualizaEstoqueDTO(
                7894900011517L,
                        3L,
                        StatusEstoqueEnum.VOLTA_PARA_O_ESTOQUE)))
                .thenReturn(
                        true
                );
        Mockito.when(this.streamBridge.send("logistica-prepara-entrega", new PreparaEntregaDTO(
                        1L,
                        "71622958004",
                        7894900011517L,
                        1L)))
                .thenReturn(
                        true
                );

        final var pedido = PedidoEntity.builder()
                .cpfCliente("71622958004")
                .ean(7894900011517L)
                .quantidade(30L)
                .statusPedido(StatusPedidoEnum.EM_TRANSPORTE)
                .dataDeCriacao(LocalDateTime.now())
                .build();
        final var pedidoSalvo = this.repository.save(pedido);

        this.mockMvc
                .perform(MockMvcRequestBuilders.delete(URL_PEDIDOS_COM_ID.replace("{idPedido}", pedidoSalvo.getId().toString()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .status()
                        .isNoContent()
                )
                .andReturn();

        final var pedidoEntity = this.repository.findAll().get(0);

        Assertions.assertEquals(1, this.repository.findAll().size());
        Assertions.assertEquals(pedidoSalvo.getId(), pedidoEntity.getId());
        Assertions.assertEquals("71622958004", pedidoEntity.getCpfCliente());
        Assertions.assertEquals(7894900011517L, pedidoEntity.getEan());
        Assertions.assertEquals(30L, pedidoEntity.getQuantidade());
        Assertions.assertEquals(StatusPedidoEnum.EM_TRANSPORTE, pedidoEntity.getStatusPedido());
        Assertions.assertNotNull(pedidoEntity.getDataDeCriacao());
        verify(streamBridge, times(0)).send(Mockito.any(), Mockito.any());
    }

    @Test
    public void cancela_deveRetornar204_naoSalvaNaBaseDeDados_pedidoNaoEncontrado() throws Exception {
        Mockito.when(this.streamBridge.send("produto-volta-estoque", new AtualizaEstoqueDTO(
                7894900011517L,
                        3L,
                        StatusEstoqueEnum.VOLTA_PARA_O_ESTOQUE)))
                .thenReturn(
                        true
                );
        Mockito.when(this.streamBridge.send("logistica-prepara-entrega", new PreparaEntregaDTO(
                        1L,
                        "71622958004",
                        7894900011517L,
                        1L)))
                .thenReturn(
                        true
                );

        this.mockMvc
                .perform(MockMvcRequestBuilders.delete(URL_PEDIDOS_COM_ID.replace("{idPedido}", "1"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .status()
                        .isNoContent()
                )
                .andReturn();

        Assertions.assertEquals(0, this.repository.findAll().size());
        verify(streamBridge, times(0)).send(Mockito.any(), Mockito.any());
    }

    @Test
    public void atualizaParaEmTransporte_deveRetornar200_salvaNaBaseDeDados() throws Exception {
        final var pedido = PedidoEntity.builder()
                .cpfCliente("71622958004")
                .ean(7894900011517L)
                .quantidade(30L)
                .statusPedido(StatusPedidoEnum.CRIADO)
                .dataDeCriacao(LocalDateTime.now())
                .build();
        final var pedidoSalvo = this.repository.save(pedido);

        this.mockMvc
                .perform(MockMvcRequestBuilders.put(URL_PEDIDOS_COM_ID.replace("{idPedido}", pedidoSalvo.getId().toString()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .status()
                        .isOk()
                )
                .andReturn();

        final var pedidoEntity = this.repository.findAll().get(0);

        Assertions.assertEquals(1, this.repository.findAll().size());
        Assertions.assertEquals(pedidoSalvo.getId(), pedidoEntity.getId());
        Assertions.assertEquals("71622958004", pedidoEntity.getCpfCliente());
        Assertions.assertEquals(7894900011517L, pedidoEntity.getEan());
        Assertions.assertEquals(30L, pedidoEntity.getQuantidade());
        Assertions.assertEquals(StatusPedidoEnum.EM_TRANSPORTE, pedidoEntity.getStatusPedido());
        Assertions.assertNotNull(pedidoEntity.getDataDeCriacao());
        verify(streamBridge, times(0)).send(Mockito.any(), Mockito.any());
    }

    @Test
    public void atualizaParaEmTransporte_deveRetornar204_naoSalvaNaBaseDeDados_statusPedidoDiferenteDeCriado() throws Exception {
        final var pedido = PedidoEntity.builder()
                .cpfCliente("71622958004")
                .ean(7894900011517L)
                .quantidade(30L)
                .statusPedido(StatusPedidoEnum.CANCELADO)
                .dataDeCriacao(LocalDateTime.now())
                .build();
        final var pedidoSalvo = this.repository.save(pedido);

        this.mockMvc
                .perform(MockMvcRequestBuilders.put(URL_PEDIDOS_COM_ID.replace("{idPedido}", pedidoSalvo.getId().toString()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .status()
                        .isNoContent()
                )
                .andReturn();

        final var pedidoEntity = this.repository.findAll().get(0);

        Assertions.assertEquals(1, this.repository.findAll().size());
        Assertions.assertEquals(pedidoSalvo.getId(), pedidoEntity.getId());
        Assertions.assertEquals("71622958004", pedidoEntity.getCpfCliente());
        Assertions.assertEquals(7894900011517L, pedidoEntity.getEan());
        Assertions.assertEquals(30L, pedidoEntity.getQuantidade());
        Assertions.assertEquals(StatusPedidoEnum.CANCELADO, pedidoEntity.getStatusPedido());
        Assertions.assertNotNull(pedidoEntity.getDataDeCriacao());
        verify(streamBridge, times(0)).send(Mockito.any(), Mockito.any());
    }

    @Test
    public void atualizaParaEmTransporte_deveRetornar204_naoSalvaNaBaseDeDados_pedidoNaoEncontrado() throws Exception {
        this.mockMvc
                .perform(MockMvcRequestBuilders.put(URL_PEDIDOS_COM_ID.replace("{idPedido}", "1"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .status()
                        .isNoContent()
                )
                .andReturn();

        Assertions.assertEquals(0, this.repository.findAll().size());
        verify(streamBridge, times(0)).send(Mockito.any(), Mockito.any());
    }

    @ParameterizedTest
    @MethodSource("requestValidandoCampos")
    public void cria_camposInvalidos_naoSalvaNaBaseDeDados(Long ean,
                                                           String cpfCliente,
                                                           Long quantidade) throws Exception {
        var request = new CriaPedidoDTO(
                ean,
                cpfCliente,
                quantidade
        );
        var objectMapper = this.objectMapper
                .writer()
                .withDefaultPrettyPrinter();
        var jsonRequest = objectMapper.writeValueAsString(request);

        this.mockMvc
                .perform(MockMvcRequestBuilders.post(URL_PEDIDOS)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .status()
                        .isBadRequest()
                );
    }

    @ParameterizedTest
    @ValueSource(longs = {
            -1000,
            -1L,
            0
    })
    public void cancela_camposInvalidos_naoSalvaNaBaseDeDados(Long idPedido) throws Exception {

        this.mockMvc
                .perform(MockMvcRequestBuilders.delete(URL_PEDIDOS_COM_ID.replace("{idPedido}", idPedido.toString()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .status()
                        .isBadRequest()
                );
    }

    @ParameterizedTest
    @ValueSource(longs = {
            -1000,
            -1L,
            0
    })
    public void atualizaParaEmTransporte_camposInvalidos_naoSalvaNaBaseDeDados(Long idPedido) throws Exception {

        this.mockMvc
                .perform(MockMvcRequestBuilders.put(URL_PEDIDOS_COM_ID.replace("{idPedido}", idPedido.toString()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .status()
                        .isBadRequest()
                );
    }

    private static Stream<Arguments> requestValidandoCampos() {
        return Stream.of(
                Arguments.of(null, "71622958004", 100L),
                Arguments.of(-1L, "71622958004", 100L),
                Arguments.of(0L, "71622958004", 100L),
                Arguments.of(123456789L, null, 100L),
                Arguments.of(123456789L, "", 100L),
                Arguments.of(123456789L, " ", 100L),
                Arguments.of(123456789L, "teste", 100L),
                Arguments.of(123456789L, "1234567891", 100L),
                Arguments.of(123456789L, "123456789123", 100L),
                Arguments.of(123456789L, "71622958004", null),
                Arguments.of(123456789L, "71622958004", -1L),
                Arguments.of(123456789L, "71622958004", 0L),
                Arguments.of(123456789L, "71622958004", 1001L)
        );
    }

}
