package org.example.avaliafit.repository;
import org.example.avaliafit.model.Paciente;
import org.example.avaliafit.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface PacienteRepository extends JpaRepository<Paciente, Integer> {

    List<Paciente> findByUsuario(Usuario usuario);
    List<Paciente> findByObjetivo(String objetivo);
    // Ensina o Spring a achar o paciente usando o ID do usuário logado
    Optional<Paciente> findByUsuario_IdUsuario(Integer idUsuario);
}
