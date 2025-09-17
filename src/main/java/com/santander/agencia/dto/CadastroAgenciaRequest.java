package com.santander.agencia.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

public record CadastroAgenciaRequest(
    
    @JsonProperty("posX")
    @NotNull(message = "Posição X é obrigatória")
    @DecimalMin(value = "-180.0", message = "Posição X deve ser maior ou igual a -180")
    @DecimalMax(value = "180.0", message = "Posição X deve ser menor ou igual a 180")
    Double posX,

    @JsonProperty("posY")
    @NotNull(message = "Posição Y é obrigatória")
    @DecimalMin(value = "-90.0", message = "Posição Y deve ser maior ou igual a -90")
    @DecimalMax(value = "90.0", message = "Posição Y deve ser menor ou igual a 90")
    Double posY
) {}
