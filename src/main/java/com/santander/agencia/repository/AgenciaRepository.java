package com.santander.agencia.repository;

import com.santander.agencia.model.Agencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface AgenciaRepository extends JpaRepository<Agencia, Long> {

    @Query(value = """
        SELECT a.id, a.nome, a.pos_x, a.pos_y, a.data_criacao,
               SQRT(POWER(a.pos_x - :posX, 2) + POWER(a.pos_y - :posY, 2)) as distancia
        FROM agencias a 
        ORDER BY distancia ASC 
        LIMIT :limite
        """, nativeQuery = true)
    List<Object[]> findAgenciasProximasComDistancia(@Param("posX") Double posX, 
                                                   @Param("posY") Double posY, 
                                                   @Param("limite") Integer limite);

    @Query(value = """
        SELECT COUNT(*) > 0
        FROM agencias a 
        WHERE SQRT(POWER(a.pos_x - :posX, 2) + POWER(a.pos_y - :posY, 2)) <= :distanciaMinima
        """, nativeQuery = true)
    boolean existsAgenciaProxima(@Param("posX") Double posX, 
                                @Param("posY") Double posY, 
                                @Param("distanciaMinima") Double distanciaMinima);

}
