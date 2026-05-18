package org.example.avaliafit.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UsuarioRequestDTO {

    private String nome;
    private LocalDate dataNascimento;
    private String email;
    private String senha;
    private String cpf;
    private String telefone;
    private String role;
    private String objetivo; // só preenchido se for paciente
    private String cargo;    // só preenchido se for funcionário
}
