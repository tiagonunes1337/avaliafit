package org.example.avaliafit.repository;

import org.example.avaliafit.model.Avaliacao;
import org.example.avaliafit.model.Funcionario;
import org.example.avaliafit.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Integer> {

    Optional<Avaliacao> findTopByPacienteOrderByDataAvaliacaoDesc(Paciente paciente);
    List<Avaliacao> findByFuncionario(Funcionario funcionario);
    List<Avaliacao> findByDataAvaliacao(LocalDateTime dataAvaliacao);
    List<Avaliacao> findByPaciente(Paciente paciente);
}
