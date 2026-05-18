package org.example.avaliafit.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class HorarioDisponivelRequestDTO {
    private Integer idFuncionario;
    private LocalDate data;
    private LocalTime horario;
}