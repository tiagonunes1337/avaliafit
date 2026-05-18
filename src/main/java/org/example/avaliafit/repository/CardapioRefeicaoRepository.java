package org.example.avaliafit.repository;

import org.example.avaliafit.model.CardapioRefeicao;
import org.example.avaliafit.model.PlanoNutricional;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface CardapioRefeicaoRepository extends JpaRepository<CardapioRefeicao, Integer> {

    List<CardapioRefeicao> findByPlanoNutricional(PlanoNutricional planoNutricional);
    List<CardapioRefeicao> findByNomeRefeicao(String nomeRefeicao);

}
