package org.example.avaliafit.repository;

import org.example.avaliafit.model.Funcionario;
import org.example.avaliafit.model.HorarioDisponivel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface HorarioDisponivelRepository extends JpaRepository<HorarioDisponivel, Integer> {

    // busca horários disponíveis de um funcionário em uma data
    List<HorarioDisponivel> findByFuncionarioAndDataAndDisponivel(
            Funcionario funcionario, LocalDate data, boolean disponivel);

    // busca todos os horários disponíveis de uma data
    List<HorarioDisponivel> findByDataAndDisponivel(LocalDate data, boolean disponivel);
}