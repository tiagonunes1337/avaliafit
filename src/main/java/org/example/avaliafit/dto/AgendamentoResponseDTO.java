package org.example.avaliafit.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AgendamentoResponseDTO {
    private Integer idAgendamento;
    private String nomePaciente;
    private String nomeFuncionario;
    private LocalDate dataConsulta;
    private LocalTime horario;
    private String tipoServico;
    private String status;
    private String observacoes;
}
