package org.example.avaliafit.repository;

import org.example.avaliafit.model.Funcionario;
import org.example.avaliafit.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FuncionarioRepository extends JpaRepository<Funcionario, Integer> {

    List<Funcionario> findByUsuario(Usuario usuario);
    List<Funcionario> findByCargo(String cargo);


}
