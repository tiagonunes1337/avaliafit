package org.example.avaliafit.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

@Data
public class UsuarioRequestDTO {

    @NotBlank(message = "O nome é obrigatório")
    private String nome;

    @NotNull(message = "A data de nascimento é obrigatória")
    private LocalDate dataNascimento;

    @Email(message = "Email inválido")
    @NotBlank(message = "O email é obrigatório")
    private String email;

    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres")
    private String senha;

    @NotBlank(message = "O CPF é obrigatório")
    private String cpf;

    @NotBlank(message = "O telefone é obrigatório")
    private String telefone;

    @NotBlank(message = "A role é obrigatória")
    private String role;

    private String objetivo; // só preenchido se for paciente
    private String cargo;    // só preenchido se for funcionário
}
