package org.example.avaliafit.repository;

import org.example.avaliafit.model.Funcionario;
import org.example.avaliafit.model.Paciente;
import org.example.avaliafit.model.PlanoNutricional;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;

public interface PlanoNutricionalRepository extends JpaRepository<PlanoNutricional, Integer> {

    List<PlanoNutricional> findByPaciente(Paciente paciente);
    List<PlanoNutricional> findByFuncionario(Funcionario funcionario);
    List<PlanoNutricional> findByAtivo(boolean ativo);

    // Pega o plano ativo atual do paciente
    Optional<PlanoNutricional> findByPacienteAndAtivo(Paciente paciente, boolean ativo);

}

