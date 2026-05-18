package org.example.avaliafit.dto;

import lombok.Data;

@Data
public class LoginResponseDTO {


    private Integer id;
    private String token;
    private String nome;
    private String role;
}
