package com.santander.agencia.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.santander.agencia.dto.CadastroAgenciaRequest;
import com.santander.agencia.dto.CadastroAgenciaResponse;
import com.santander.agencia.dto.DistanciaResponse;
import com.santander.agencia.model.Agencia;
import com.santander.agencia.service.AgenciaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do Controller AgenciaController")
class AgenciaControllerTest {

    @Mock
    private AgenciaService agenciaService;

    @InjectMocks
    private AgenciaController agenciaController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(agenciaController)
                .setControllerAdvice(new com.santander.agencia.exception.GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Deve cadastrar agência com sucesso")
    void deveCadastrarAgenciaComSucesso() throws Exception {
        CadastroAgenciaRequest request = new CadastroAgenciaRequest(10.0, -5.0);
        CadastroAgenciaResponse response = new CadastroAgenciaResponse(
                1L,
                "AGENCIA_1",
                10.0,
                -5.0,
                LocalDateTime.now(),
                "Agência cadastrada com sucesso!"
        );

        when(agenciaService.cadastrarAgencia(any(CadastroAgenciaRequest.class))).thenReturn(response);

        mockMvc.perform(post("/desafio/cadastrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("AGENCIA_1"))
                .andExpect(jsonPath("$.posX").value(10.0))
                .andExpect(jsonPath("$.posY").value(-5.0))
                .andExpect(jsonPath("$.mensagem").value("Agência cadastrada com sucesso!"));

        verify(agenciaService).cadastrarAgencia(any(CadastroAgenciaRequest.class));
    }

    @Test
    @DisplayName("Deve retornar erro 400 para dados inválidos")
    void deveRetornarErro400ParaDadosInvalidos() throws Exception {
        CadastroAgenciaRequest request = new CadastroAgenciaRequest(null, null); // posX e posY null

        mockMvc.perform(post("/desafio/cadastrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(agenciaService, never()).cadastrarAgencia(any());
    }

    @Test
    @DisplayName("Deve buscar agências próximas com sucesso")
    void deveBuscarAgenciasProximasComSucesso() throws Exception {
        DistanciaResponse response = new DistanciaResponse(
            new DistanciaResponse.PosicaoUsuario(-10.0, 5.0),
            Map.of(
                "AGENCIA_2", "distancia = 2.20",
                "AGENCIA_1", "distancia = 10.00", 
                "AGENCIA_3", "distancia = 37.42"
            ),
            3,
            "AGENCIA_2",
            2.2
        );

        when(agenciaService.buscarAgenciasProximas(-10.0, 5.0)).thenReturn(response);

        mockMvc.perform(get("/desafio/distancia")
                .param("posX", "-10.0")
                .param("posY", "5.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posicaoUsuario.posX").value(-10.0))
                .andExpect(jsonPath("$.posicaoUsuario.posY").value(5.0))
                .andExpect(jsonPath("$.totalAgencias").value(3))
                .andExpect(jsonPath("$.agenciaMaisProxima").value("AGENCIA_2"))
                .andExpect(jsonPath("$.menorDistancia").value(2.2))
                .andExpect(jsonPath("$.agencias.AGENCIA_2").value("distancia = 2.20"))
                .andExpect(jsonPath("$.agencias.AGENCIA_1").value("distancia = 10.00"))
                .andExpect(jsonPath("$.agencias.AGENCIA_3").value("distancia = 37.42"));

        verify(agenciaService).buscarAgenciasProximas(-10.0, 5.0);
    }

    @Test
    @DisplayName("Deve retornar erro 500 para exceção interna")
    void deveRetornarErro500ParaExcecaoInterna() throws Exception {
        when(agenciaService.buscarAgenciasProximas(anyDouble(), anyDouble()))
            .thenThrow(new RuntimeException("Erro interno"));

        mockMvc.perform(get("/desafio/distancia")
                .param("posX", "0.0")
                .param("posY", "0.0"))
                .andExpect(status().isInternalServerError());

        verify(agenciaService).buscarAgenciasProximas(0.0, 0.0);
    }

    @Test
    @DisplayName("Deve buscar agência por ID com sucesso")
    void deveBuscarAgenciaPorIdComSucesso() throws Exception {
        Agencia agencia = Agencia.builder()
                .id(1L)
                .nome("AGENCIA_1")
                .posX(10.0)
                .posY(-5.0)
                .dataCriacao(LocalDateTime.now())
                .build();

        when(agenciaService.buscarAgenciaPorId(1L)).thenReturn(agencia);

        mockMvc.perform(get("/desafio/agencias/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("AGENCIA_1"))
                .andExpect(jsonPath("$.posX").value(10.0))
                .andExpect(jsonPath("$.posY").value(-5.0));

        verify(agenciaService).buscarAgenciaPorId(1L);
    }

    @Test
    @DisplayName("Deve retornar erro 500 quando agência não for encontrada")
    void deveRetornarErro500QuandoAgenciaNaoForEncontrada() throws Exception {
        when(agenciaService.buscarAgenciaPorId(999L))
                .thenThrow(new RuntimeException("Agência não encontrada com ID: 999"));

        mockMvc.perform(get("/desafio/agencias/999"))
                .andExpect(status().isInternalServerError());

        verify(agenciaService).buscarAgenciaPorId(999L);
    }

    @Test
    @DisplayName("Deve retornar erro 500 para ID inválido")
    void deveRetornarErro500ParaIdInvalido() throws Exception {
        when(agenciaService.buscarAgenciaPorId(null))
                .thenThrow(new IllegalArgumentException("ID da agência é obrigatório"));

        mockMvc.perform(get("/desafio/agencias/null"))
                .andExpect(status().isBadRequest());

        verify(agenciaService, never()).buscarAgenciaPorId(any());
    }

    @Test
    @DisplayName("Deve retornar erro 400 quando tentar cadastrar agência muito próxima")
    void deveRetornarErro400QuandoTentarCadastrarAgenciaMuitoProxima() throws Exception {
        CadastroAgenciaRequest request = new CadastroAgenciaRequest(10.0, -5.0);
        
        when(agenciaService.cadastrarAgencia(any(CadastroAgenciaRequest.class)))
            .thenThrow(new IllegalArgumentException("Já existe uma agência próxima a esta posição. Distância mínima permitida: 1,0 unidades"));

        mockMvc.perform(post("/desafio/cadastrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(agenciaService).cadastrarAgencia(any(CadastroAgenciaRequest.class));
    }
}
