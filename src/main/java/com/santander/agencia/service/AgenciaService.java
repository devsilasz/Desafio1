package com.santander.agencia.service;

import com.santander.agencia.dto.CadastroAgenciaRequest;
import com.santander.agencia.dto.CadastroAgenciaResponse;
import com.santander.agencia.dto.DistanciaResponse;
import com.santander.agencia.model.Agencia;
import com.santander.agencia.repository.AgenciaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class AgenciaService {

    private static final Logger logger = LoggerFactory.getLogger(AgenciaService.class);
    private static final Double DISTANCIA_MINIMA_ENTRE_AGENCIAS = 1.0;

    @Autowired
    private AgenciaRepository agenciaRepository;

    @Transactional
    public CadastroAgenciaResponse cadastrarAgencia(CadastroAgenciaRequest request) {
        if (request == null || request.posX() == null || request.posY() == null) {
            throw new IllegalArgumentException("Parâmetros posX e posY são obrigatórios");
        }
        
        logger.info("Iniciando cadastro de agência na posição ({}, {})", request.posX(), request.posY());

        if (agenciaRepository.existsAgenciaProxima(request.posX(), request.posY(), DISTANCIA_MINIMA_ENTRE_AGENCIAS)) {
            logger.warn("Tentativa de cadastro de agência muito próxima a uma existente na posição ({}, {})", 
                       request.posX(), request.posY());
            throw new IllegalArgumentException(
                String.format("Já existe uma agência próxima a esta posição. Distância mínima permitida: %.1f unidades", 
                             DISTANCIA_MINIMA_ENTRE_AGENCIAS)
            );
        }

        long proximoId = agenciaRepository.count() + 1;
        String nomeAgencia = "AGENCIA_" + proximoId;

        Agencia agencia = Agencia.builder()
                .nome(nomeAgencia)
                .posX(request.posX())
                .posY(request.posY())
                .build();
        agencia = agenciaRepository.save(agencia);

        logger.info("Agência cadastrada com sucesso - ID: {}, Nome: {}", agencia.getId(), agencia.getNome());

        return new CadastroAgenciaResponse(
                agencia.getId(),
                agencia.getNome(),
                agencia.getPosX(),
                agencia.getPosY(),
                agencia.getDataCriacao(),
                "Agência cadastrada com sucesso!"
        );
    }

    @Transactional(readOnly = true)
    public DistanciaResponse buscarAgenciasProximas(Double posX, Double posY) {
        if (posX == null || posY == null) {
            throw new IllegalArgumentException("Parâmetros posX e posY são obrigatórios");
        }
        
        logger.info("Buscando agências próximas à posição ({}, {})", posX, posY);

        try {
            List<Object[]> resultados = agenciaRepository.findAgenciasProximasComDistancia(
                posX, posY, 1000
            );

            DistanciaResponse response = processarResultadosAgencias(resultados, posX, posY);

            logger.info("Encontradas {} agências próximas à posição ({}, {})", 
                       response.totalAgencias(), posX, posY);

            return response;

        } catch (Exception e) {
            logger.error("Erro ao buscar agências próximas: {}", e.getMessage(), e);
            throw new RuntimeException("Erro interno ao buscar agências próximas", e);
        }
    }


    private DistanciaResponse processarResultadosAgencias(List<Object[]> resultados, Double posX, Double posY) {
        Map<String, String> agencias = new LinkedHashMap<>();
        String agenciaMaisProxima = null;
        Double menorDistancia = null;

        for (Object[] resultado : resultados) {
            Agencia agencia = construirAgencia(resultado);
            Double distancia = ((Number) resultado[5]).doubleValue();
            
            String distanciaFormatada = formatarDistancia(distancia);
            String nomeAgencia = agencia.getNome();
            agencias.put(nomeAgencia, distanciaFormatada);
            
            if (agenciaMaisProxima == null || distancia < menorDistancia) {
                agenciaMaisProxima = nomeAgencia;
                menorDistancia = distancia;
            }
        }

        return new DistanciaResponse(
            new DistanciaResponse.PosicaoUsuario(posX, posY),
            agencias,
            agencias.size(),
            agenciaMaisProxima,
            menorDistancia
        );
    }

    private Agencia construirAgencia(Object[] resultado) {
        return Agencia.builder()
            .id(((Number) resultado[0]).longValue())
            .nome((String) resultado[1])
            .posX(((Number) resultado[2]).doubleValue())
            .posY(((Number) resultado[3]).doubleValue())
            .dataCriacao(((java.sql.Timestamp) resultado[4]).toLocalDateTime())
            .build();
    }

    private String formatarDistancia(Double distancia) {
        return String.format("distancia = %.2f", distancia).replace(",", ".");
    }


    public double calcularDistancia(Agencia agencia, double posX, double posY) {
        double deltaX = agencia.getPosX() - posX;
        double deltaY = agencia.getPosY() - posY;
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

    public String obterNomeAgencia(Agencia agencia) {
        return "AGENCIA_" + (agencia.getId() != null ? agencia.getId() : "NOVA");
    }

    @Transactional(readOnly = true)
    public Agencia buscarAgenciaPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID da agência é obrigatório");
        }
        
        logger.info("Buscando agência com ID: {}", id);
        
        Optional<Agencia> agenciaOptional = agenciaRepository.findById(id);
        
        if (agenciaOptional.isEmpty()) {
            logger.warn("Agência não encontrada com ID: {}", id);
            throw new RuntimeException("Agência não encontrada com ID: " + id);
        }
        
        Agencia agencia = agenciaOptional.get();
        logger.info("Agência encontrada - ID: {}, Nome: {}", agencia.getId(), agencia.getNome());
        
        return agencia;
    }
}
