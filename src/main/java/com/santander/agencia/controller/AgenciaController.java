package com.santander.agencia.controller;

import com.santander.agencia.dto.CadastroAgenciaRequest;
import com.santander.agencia.dto.CadastroAgenciaResponse;
import com.santander.agencia.dto.DistanciaResponse;
import com.santander.agencia.model.Agencia;
import com.santander.agencia.service.AgenciaService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/desafio")
@CrossOrigin(origins = "*")
public class AgenciaController {

    private static final Logger logger = LoggerFactory.getLogger(AgenciaController.class);

    @Autowired
    private AgenciaService agenciaService;

    @PostMapping("/cadastrar")
    public ResponseEntity<CadastroAgenciaResponse> cadastrarAgencia(
            @Valid @RequestBody CadastroAgenciaRequest request) {
        
        logger.info("Recebida requisição para cadastrar agência na posição ({}, {})", 
                   request.posX(), request.posY());

        CadastroAgenciaResponse response = agenciaService.cadastrarAgencia(request);
        
        logger.info("Agência cadastrada com sucesso - ID: {}", response.id());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/distancia")
    public ResponseEntity<DistanciaResponse> buscarAgenciasProximas(
            @RequestParam(value = "posX", required = true) Double posX,
            @RequestParam(value = "posY", required = true) Double posY) {
        
        logger.info("Recebida requisição para buscar agências próximas à posição ({}, {})", posX, posY);

        DistanciaResponse response = agenciaService.buscarAgenciasProximas(posX, posY);
        
        logger.info("Consulta realizada com sucesso - {} agências encontradas", 
                   response.totalAgencias());
        
        return ResponseEntity.ok(response);
    }
}
