package org.example.avaliafit.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AvaliacaoResponseDTO {

    private Integer idAvaliacao;
    private Integer idAgendamento;
    private String nomePaciente;// vem do Usuario dentro do Paciente
    private String nomeFuncionario;// vem do Usuario dentro do Funcionario
    private LocalDateTime dataAvaliacao;
    private BigDecimal peso;
    private BigDecimal altura;
    private BigDecimal imc;
    private BigDecimal percentualGordura;
    private BigDecimal massaMuscular;
    private String observacoes;


}
