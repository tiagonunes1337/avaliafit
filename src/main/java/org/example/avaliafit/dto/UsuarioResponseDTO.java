package org.example.avaliafit.dto;

import lombok.Data;

@Data
public class UsuarioResponseDTO {

    private Integer id;
    private String nome;
    private String email;
    private String cpf;
    private String telefone;
    private String role;

}
