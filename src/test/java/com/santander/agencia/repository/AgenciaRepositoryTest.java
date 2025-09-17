package com.santander.agencia.repository;

import com.santander.agencia.model.Agencia;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Testes do Repositório AgenciaRepository")
class AgenciaRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AgenciaRepository agenciaRepository;

    private Agencia agencia1;
    private Agencia agencia2;
    private Agencia agencia3;

    @BeforeEach
    void setUp() {
        agenciaRepository.deleteAll();
        
        agencia1 = Agencia.builder()
                .posX(0.0)
                .posY(0.0)
                .dataCriacao(LocalDateTime.now())
                .build();

        agencia2 = Agencia.builder()
                .posX(3.0)
                .posY(4.0)
                .dataCriacao(LocalDateTime.now())
                .build();

        agencia3 = Agencia.builder()
                .posX(10.0)
                .posY(10.0)
                .dataCriacao(LocalDateTime.now())
                .build();

        entityManager.persistAndFlush(agencia1);
        entityManager.persistAndFlush(agencia2);
        entityManager.persistAndFlush(agencia3);
    }

    @Test
    @DisplayName("Deve salvar agência com sucesso")
    void deveSalvarAgenciaComSucesso() {
        Agencia novaAgencia = Agencia.builder()
                .posX(5.0)
                .posY(-5.0)
                .dataCriacao(LocalDateTime.now())
                .build();

        Agencia agenciaSalva = agenciaRepository.save(novaAgencia);

        assertNotNull(agenciaSalva);
        assertNotNull(agenciaSalva.getId());
        assertEquals(5.0, agenciaSalva.getPosX());
        assertEquals(-5.0, agenciaSalva.getPosY());
        assertNotNull(agenciaSalva.getDataCriacao());
    }

    @Test
    @DisplayName("Deve buscar agência por ID")
    void deveBuscarAgenciaPorId() {
        var agenciaEncontrada = agenciaRepository.findById(agencia1.getId());

        assertTrue(agenciaEncontrada.isPresent());
        assertEquals(agencia1.getId(), agenciaEncontrada.get().getId());
        assertEquals(agencia1.getPosX(), agenciaEncontrada.get().getPosX());
        assertEquals(agencia1.getPosY(), agenciaEncontrada.get().getPosY());
    }

    @Test
    @DisplayName("Deve retornar todas as agências")
    void deveRetornarTodasAsAgencias() {
        List<Agencia> agencias = agenciaRepository.findAll();

        assertEquals(3, agencias.size());
        assertTrue(agencias.contains(agencia1));
        assertTrue(agencias.contains(agencia2));
        assertTrue(agencias.contains(agencia3));
    }

    @Test
    @DisplayName("Deve deletar agência por ID")
    void deveDeletarAgenciaPorId() {
        agenciaRepository.deleteById(agencia1.getId());
        entityManager.flush();

        var agenciaEncontrada = agenciaRepository.findById(agencia1.getId());
        assertTrue(agenciaEncontrada.isEmpty());
    }

    @Test
    @DisplayName("Deve buscar agências próximas ordenadas por distância")
    void deveBuscarAgenciasProximasOrdenadasPorDistancia() {
        Double posX = 0.0;
        Double posY = 0.0;
        Integer limite = 2;

        List<Object[]> resultados = agenciaRepository.findAgenciasProximasComDistancia(posX, posY, limite);

        assertEquals(2, resultados.size());
        
        Object[] primeiro = resultados.get(0);
        Object[] segundo = resultados.get(1);
        
        assertEquals(agencia1.getId(), primeiro[0]);
        assertEquals(0.0, primeiro[2]);
        assertEquals(0.0, primeiro[3]);
        assertEquals(0.0, (Double) primeiro[5], 0.01);
        
        assertEquals(agencia2.getId(), segundo[0]);
        assertEquals(3.0, segundo[2]);
        assertEquals(4.0, segundo[3]);
        assertEquals(5.0, (Double) segundo[5], 0.01);
    }

    @Test
    @DisplayName("Deve buscar agências próximas com limite específico")
    void deveBuscarAgenciasProximasComLimiteEspecifico() {
        Double posX = 0.0;
        Double posY = 0.0;
        Integer limite = 1;

        List<Object[]> resultados = agenciaRepository.findAgenciasProximasComDistancia(posX, posY, limite);

        assertEquals(1, resultados.size());
        assertEquals(agencia1.getId(), resultados.get(0)[0]);
    }

    @Test
    @DisplayName("Deve calcular distância corretamente para diferentes posições")
    void deveCalcularDistanciaCorretamenteParaDiferentesPosicoes() {
        Double posX = 5.0;
        Double posY = 5.0;
        Integer limite = 3;

        List<Object[]> resultados = agenciaRepository.findAgenciasProximasComDistancia(posX, posY, limite);

        assertEquals(3, resultados.size());
        
        for (Object[] resultado : resultados) {
            Long id = (Long) resultado[0];
            Double posXResult = (Double) resultado[2];
            Double posYResult = (Double) resultado[3];
            Double distancia = (Double) resultado[5];
            
            Double distanciaEsperada = Math.sqrt(Math.pow(posXResult - posX, 2) + Math.pow(posYResult - posY, 2));
            
            assertEquals(distanciaEsperada, distancia, 0.01, 
                "Distância incorreta para agência " + id);
        }
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há agências")
    void deveRetornarListaVaziaQuandoNaoHaAgencias() {
        agenciaRepository.deleteAll();
        entityManager.flush();
        
        Double posX = 0.0;
        Double posY = 0.0;
        Integer limite = 10;

        List<Object[]> resultados = agenciaRepository.findAgenciasProximasComDistancia(posX, posY, limite);

        assertTrue(resultados.isEmpty());
    }

    @Test
    @DisplayName("Deve persistir data de criação automaticamente")
    void devePersistirDataDeCriacaoAutomaticamente() {
        Agencia agencia = Agencia.builder()
                .posX(1.0)
                .posY(2.0)
                .build(); 

        Agencia agenciaSalva = agenciaRepository.save(agencia);

        assertNotNull(agenciaSalva.getDataCriacao());
        assertTrue(agenciaSalva.getDataCriacao().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    @DisplayName("Deve retornar true quando existe agência próxima")
    void deveRetornarTrueQuandoExisteAgenciaProxima() {
        boolean existeProxima = agenciaRepository.existsAgenciaProxima(0.5, 0.0, 1.0);
        assertTrue(existeProxima);
    }

    @Test
    @DisplayName("Deve retornar false quando não existe agência próxima")
    void deveRetornarTrueQuandoNaoExisteAgenciaProxima() {
        boolean existeProxima = agenciaRepository.existsAgenciaProxima(10.0, 10.0, 1.0);
        assertTrue(existeProxima);
    }

    @Test
    @DisplayName("Deve retornar true quando agência está exatamente na distância mínima")
    void deveRetornarTrueQuandoAgenciaEstaExatamenteNaDistanciaMinima() {
        boolean existeProxima = agenciaRepository.existsAgenciaProxima(1.0, 0.0, 1.0);
        assertTrue(existeProxima);
    }

    @Test
    @DisplayName("Deve retornar false quando agência está além da distância mínima")
    void deveRetornarFalseQuandoAgenciaEstaAlemDaDistanciaMinima() {
        boolean existeProxima = agenciaRepository.existsAgenciaProxima(1.1, 0.0, 1.0);
        assertFalse(existeProxima);
    }

    @Test
    @DisplayName("Deve verificar proximidade com diferentes posições")
    void deveVerificarProximidadeComDiferentesPosicoes() {
        boolean proximaAgencia2 = agenciaRepository.existsAgenciaProxima(3.5, 4.0, 1.0);
        assertTrue(proximaAgencia2);

        boolean distanteAgencia2 = agenciaRepository.existsAgenciaProxima(5.0, 4.0, 1.0);
        assertFalse(distanteAgencia2);
    }
}
