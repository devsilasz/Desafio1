package com.santander.agencia.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "agencias", indexes = {
    @Index(name = "idx_posicao", columnList = "pos_x, pos_y"),
    @Index(name = "idx_data_criacao", columnList = "data_criacao")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Agencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pos_x", nullable = false)
    @NotNull(message = "Posição X é obrigatória")
    private Double posX;

    @Column(name = "pos_y", nullable = false)
    @NotNull(message = "Posição Y é obrigatória")
    private Double posY;

    @Column(name = "nome", nullable = true, length = 100)
    private String nome;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime dataCriacao = LocalDateTime.now();

}
