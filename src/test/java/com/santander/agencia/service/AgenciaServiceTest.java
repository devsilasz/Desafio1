package com.santander.agencia.service;

import com.santander.agencia.dto.CadastroAgenciaRequest;
import com.santander.agencia.dto.CadastroAgenciaResponse;
import com.santander.agencia.dto.DistanciaResponse;
import com.santander.agencia.model.Agencia;
import com.santander.agencia.repository.AgenciaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do Serviço AgenciaService")
class AgenciaServiceTest {

    @Mock
    private AgenciaRepository agenciaRepository;

    @InjectMocks
    private AgenciaService agenciaService;

    private Agencia agencia;
    private CadastroAgenciaRequest request;

    @BeforeEach
    void setUp() {
        agencia = Agencia.builder()
                .id(1L)
                .nome("AGENCIA_1")
                .posX(10.0)
                .posY(-5.0)
                .dataCriacao(LocalDateTime.now())
                .build();

        request = new CadastroAgenciaRequest(10.0, -5.0);
    }

    @Test
    @DisplayName("Deve cadastrar agência com sucesso")
    void deveCadastrarAgenciaComSucesso() {
        when(agenciaRepository.count()).thenReturn(0L);
        when(agenciaRepository.save(any(Agencia.class))).thenReturn(agencia);

        CadastroAgenciaResponse response = agenciaService.cadastrarAgencia(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("AGENCIA_1", response.nome());
        assertEquals(10.0, response.posX());
        assertEquals(-5.0, response.posY());
        assertEquals("Agência cadastrada com sucesso!", response.mensagem());

        verify(agenciaRepository).count();
        verify(agenciaRepository).save(any(Agencia.class));
    }

    @Test
    @DisplayName("Deve cadastrar agência com posições válidas")
    void deveCadastrarAgenciaComPosicoesValidas() {
        request = new CadastroAgenciaRequest(15.0, -10.0);
        when(agenciaRepository.count()).thenReturn(0L);
        when(agenciaRepository.save(any(Agencia.class))).thenReturn(agencia);

        CadastroAgenciaResponse response = agenciaService.cadastrarAgencia(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("AGENCIA_1", response.nome());
        assertEquals(10.0, response.posX());
        assertEquals(-5.0, response.posY());

        verify(agenciaRepository).count();
        verify(agenciaRepository).save(any(Agencia.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando posições são inválidas")
    void deveLancarExcecaoQuandoPosicoesSaoInvalidas() {
        CadastroAgenciaRequest requestInvalido = new CadastroAgenciaRequest(null, -5.0);

        assertThrows(
            IllegalArgumentException.class,
            () -> agenciaService.cadastrarAgencia(requestInvalido)
        );

        verify(agenciaRepository, never()).save(any(Agencia.class));
    }

    @Test
    @DisplayName("Deve buscar agências próximas com sucesso")
    void deveBuscarAgenciasProximasComSucesso() {
        Object[] resultado1 = {1L, "AGENCIA_1", 0.0, 0.0, java.sql.Timestamp.valueOf(LocalDateTime.now()), 5.0};
        Object[] resultado2 = {2L, "AGENCIA_2", 5.0, 5.0, java.sql.Timestamp.valueOf(LocalDateTime.now()), 7.07};

        when(agenciaRepository.findAgenciasProximasComDistancia(0.0, 0.0, 1000))
            .thenReturn(Arrays.asList(resultado1, resultado2));

        DistanciaResponse response = agenciaService.buscarAgenciasProximas(0.0, 0.0);

        assertNotNull(response);
        assertEquals(2, response.totalAgencias());
        assertEquals("AGENCIA_1", response.agenciaMaisProxima());
        assertEquals(5.0, response.menorDistancia(), 0.01);
        assertTrue(response.agencias().containsKey("AGENCIA_1"));
        assertTrue(response.agencias().containsKey("AGENCIA_2"));
        assertTrue(response.agencias().get("AGENCIA_1").contains("5.00"));
        assertTrue(response.agencias().get("AGENCIA_2").contains("7.07"));

        verify(agenciaRepository).findAgenciasProximasComDistancia(0.0, 0.0, 1000);
    }

    @Test
    @DisplayName("Deve buscar agência por ID com sucesso")
    void deveBuscarAgenciaPorIdComSucesso() {
        when(agenciaRepository.findById(1L)).thenReturn(Optional.of(agencia));

        Agencia resultado = agenciaService.buscarAgenciaPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("AGENCIA_1", resultado.getNome());
        assertEquals(10.0, resultado.getPosX());
        assertEquals(-5.0, resultado.getPosY());

        verify(agenciaRepository).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção quando agência não for encontrada")
    void deveLancarExcecaoQuandoAgenciaNaoForEncontrada() {
        when(agenciaRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> agenciaService.buscarAgenciaPorId(999L)
        );

        assertEquals("Agência não encontrada com ID: 999", exception.getMessage());
        verify(agenciaRepository).findById(999L);
    }

    @Test
    @DisplayName("Deve lançar exceção quando ID for nulo")
    void deveLancarExcecaoQuandoIdForNulo() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> agenciaService.buscarAgenciaPorId(null)
        );

        assertEquals("ID da agência é obrigatório", exception.getMessage());
        verify(agenciaRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando tentar cadastrar agência muito próxima")
    void deveLancarExcecaoQuandoTentarCadastrarAgenciaMuitoProxima() {
        when(agenciaRepository.existsAgenciaProxima(10.0, -5.0, 1.0)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> agenciaService.cadastrarAgencia(request)
        );

        assertEquals("Já existe uma agência próxima a esta posição. Distância mínima permitida: 1,0 unidades", 
                    exception.getMessage());
        verify(agenciaRepository).existsAgenciaProxima(10.0, -5.0, 1.0);
        verify(agenciaRepository, never()).save(any(Agencia.class));
    }

    @Test
    @DisplayName("Deve permitir cadastro quando não há agência próxima")
    void devePermitirCadastroQuandoNaoHaAgenciaProxima() {
        when(agenciaRepository.existsAgenciaProxima(10.0, -5.0, 1.0)).thenReturn(false);
        when(agenciaRepository.count()).thenReturn(0L);
        when(agenciaRepository.save(any(Agencia.class))).thenReturn(agencia);

        CadastroAgenciaResponse response = agenciaService.cadastrarAgencia(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("AGENCIA_1", response.nome());
        verify(agenciaRepository).existsAgenciaProxima(10.0, -5.0, 1.0);
        verify(agenciaRepository).save(any(Agencia.class));
    }

    @Test
    @DisplayName("Deve permitir cadastro quando agência está na distância mínima exata")
    void devePermitirCadastroQuandoAgenciaEstaNaDistanciaMinimaExata() {
        CadastroAgenciaRequest requestDistanciaExata = new CadastroAgenciaRequest(11.0, -5.0);
        when(agenciaRepository.existsAgenciaProxima(11.0, -5.0, 1.0)).thenReturn(false);
        when(agenciaRepository.count()).thenReturn(0L);
        when(agenciaRepository.save(any(Agencia.class))).thenReturn(agencia);

        CadastroAgenciaResponse response = agenciaService.cadastrarAgencia(requestDistanciaExata);

        assertNotNull(response);
        verify(agenciaRepository).existsAgenciaProxima(11.0, -5.0, 1.0);
        verify(agenciaRepository).save(any(Agencia.class));
    }
}
