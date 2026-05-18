package org.example.avaliafit.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AgendamentoRequestDTO {

    private Integer idPaciente;
    private Integer idFuncionario;
    private Integer idHorario;
    private LocalDate dataConsulta;
    private LocalTime horario;
    private String tipoServico;
    private String observacoes;

}
