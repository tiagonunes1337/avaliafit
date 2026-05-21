package org.example.avaliafit.dto;

import lombok.Data;

import java.time.LocalDate;
@Data
public class PlanoNutricionalRequestDTO {


    private Integer idFuncionario;
    private Integer idPaciente;
    private Double kcalDiario;
    private Double proteinas;
    private Double carboidratos;
    private Double gorduras;
    private Double pesoObjetivo;
    private Double metaAguaLitros;
    private LocalDate dataInicio;
    private boolean ativo;
}
