package org.example.avaliafit.dto;

import lombok.Data;
import java.time.LocalDate;
@Data
public class PlanoNutricionalResponseDTO {

    private Integer idPlano;
    private int kcalDiario;
    private int proteinas;
    private int carboidratos;
    private int gorduras;
    private LocalDate dataInicio;
    private boolean ativo;
}
