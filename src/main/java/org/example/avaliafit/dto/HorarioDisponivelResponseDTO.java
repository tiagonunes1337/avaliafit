package org.example.avaliafit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HorarioDisponivelResponseDTO {
    private Integer idHorario;
    private LocalDate data;
    private LocalTime horario;
    private Boolean disponivel;
    private FuncionarioInfoDTO funcionario;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FuncionarioInfoDTO {
        private Integer idFuncionario;
        private UsuarioInfoDTO usuario;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class UsuarioInfoDTO {
            private Integer idUsuario;
            private String nome;
            private String email;
            private String role;
        }
    }
}