package org.example.avaliafit.repository;

import org.example.avaliafit.model.Agendamento;
import org.example.avaliafit.model.Funcionario;
import org.example.avaliafit.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Integer> {

    List<Agendamento> findByPaciente(Paciente paciente);
    List<Agendamento> findByFuncionario(Funcionario funcionario);
    List<Agendamento> findByDataConsulta(LocalDate dataConsulta);
    List<Agendamento> findByHorario(LocalTime horario);
    List<Agendamento> findByStatus(String status);
    List<Agendamento> findByDataConsultaLessThanEqualAndAvaliacaoIsNull(LocalDate data);

}
