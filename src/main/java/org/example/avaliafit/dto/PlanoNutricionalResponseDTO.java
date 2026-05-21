package org.example.avaliafit.dto;

import lombok.Data;
import java.time.LocalDate;
@Data
public class PlanoNutricionalResponseDTO {

    private Integer idPlano;
    private double kcalDiario;
    private double proteinas;
    private double carboidratos;
    private double gorduras;
    private LocalDate dataInicio;
    private boolean ativo;
    private Double pesoObjetivo;
    private Double metaAguaLitros;
    private PacienteInfoDTO paciente;
    private ProfissionalInfoDTO profissional;


    @Data
    public static class PacienteInfoDTO {
        private Integer idUsuario; // ID do Usuário para facilitar a busca no Front-End
        private String nome;

    }

    @Data
    public static class ProfissionalInfoDTO {
        private Integer idFuncionario;
        private String nome;
    }


}
