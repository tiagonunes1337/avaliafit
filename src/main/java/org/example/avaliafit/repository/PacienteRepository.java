package org.example.avaliafit.repository;
import org.example.avaliafit.model.Paciente;
import org.example.avaliafit.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface PacienteRepository extends JpaRepository<Paciente, Integer> {

    List<Paciente> findByUsuario(Usuario usuario);
    List<Paciente> findByObjetivo(String objetivo);
}
