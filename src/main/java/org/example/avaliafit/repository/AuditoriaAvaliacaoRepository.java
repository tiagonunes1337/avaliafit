package org.example.avaliafit.repository;


import org.example.avaliafit.model.AuditoriaAvaliacao;
import org.example.avaliafit.model.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditoriaAvaliacaoRepository extends JpaRepository<AuditoriaAvaliacao, Integer> {

    List<AuditoriaAvaliacao> findByAvaliacaoOrderByDataAlteracaoDesc(Avaliacao avaliacao);

    List<AuditoriaAvaliacao> findByAvaliacaoAndCampoAlterado(Avaliacao avaliacao, String campoAlterado);
}

