package com.fiap.techchallenge4.integrados;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.techchallenge4.domain.StatusPedidoEnum;
import com.fiap.techchallenge4.infrastructure.controller.dto.BaixaNoEstoqueDTO;
import com.fiap.techchallenge4.infrastructure.controller.dto.CriaPedidoDTO;
import com.fiap.techchallenge4.infrastructure.produto.client.ProdutoClient;
import com.fiap.techchallenge4.infrastructure.repository.PedidoRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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

import java.util.stream.Stream;

import static com.fiap.techchallenge4.infrastructure.controller.PedidoController.URL_PEDIDOS;
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
    ProdutoClient client;

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
        Mockito.when(this.client.temEstoque(7894900011517L, 3L))
                .thenReturn(
                        true
                );
        Mockito.when(this.streamBridge.send("produto-atualiza-estoque", new BaixaNoEstoqueDTO(7894900011517L, 3L)))
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
    }

    @Test
    public void cria_deveRetornar409_naoSalvaNaBaseDeDados_semEstoque() throws Exception {
        Mockito.when(this.client.temEstoque(7894900011517L, 3L))
                .thenReturn(
                        false
                );
        Mockito.when(this.streamBridge.send("produto-atualiza-estoque", new BaixaNoEstoqueDTO(7894900011517L, 3L)))
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
    public void cria_deveRetornar409_naoSalvaNaBaseDeDados_apiIndisponivel() throws Exception {
        Mockito.doThrow(
                        new RuntimeException("API INDISPONIVEL!!")
                )
                .when(this.client)
                .temEstoque(7894900011517L, 3L);
        Mockito.when(this.streamBridge.send("produto-atualiza-estoque", new BaixaNoEstoqueDTO(7894900011517L, 3L)))
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
        Mockito.when(this.client.temEstoque(7894900011517L, 3L))
                .thenReturn(
                        null
                );
        Mockito.when(this.streamBridge.send("produto-atualiza-estoque", new BaixaNoEstoqueDTO(7894900011517L, 3L)))
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

    private static Stream<Arguments> requestValidandoCampos() {
        return Stream.of(
                Arguments.of(null, "71622958004", 100L),
                Arguments.of(-1L, "71622958004", 100L),
                Arguments.of(0L, "71622958004", 100L),
                Arguments.of(0L, null, 100L),
                Arguments.of(0L, "", 100L),
                Arguments.of(0L, " ", 100L),
                Arguments.of(0L, "teste", 100L),
                Arguments.of(0L, "1234567891", 100L),
                Arguments.of(0L, "123456789123", 100L),
                Arguments.of(123456789L, "71622958004", null),
                Arguments.of(123456789L, "71622958004", -1L),
                Arguments.of(123456789L, "71622958004", 0L),
                Arguments.of(123456789L, "71622958004", 1001L)
        );
    }

}
