package com.santander.agencia.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@Schema(description = "Resposta da consulta de distâncias das agências")
public record DistanciaResponse(
    
    @JsonProperty("posicaoUsuario")
    PosicaoUsuario posicaoUsuario,

    @JsonProperty("agencias")
    Map<String, String> agencias,

    @JsonProperty("totalAgencias")
    Integer totalAgencias,

    @JsonProperty("agenciaMaisProxima")
    String agenciaMaisProxima,

    @JsonProperty("menorDistancia")
    Double menorDistancia
) {

    public record PosicaoUsuario(
        
        @JsonProperty("posX")
        Double posX,

        @JsonProperty("posY")
        Double posY
    ) {}
}
