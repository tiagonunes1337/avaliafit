package org.example.avaliafit.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AvaliacaoResponseDTO {

    private Integer       idAvaliacao;
    private Integer       idAgendamento;
    private String        nomePaciente;
    private String        nomeFuncionario;
    private LocalDateTime dataAvaliacao;

    // Medidas
    private BigDecimal peso;
    private BigDecimal altura;
    private BigDecimal imc;
    private BigDecimal percentualGordura;
    private BigDecimal massaMuscular;
    private String     observacoes;

    // Calculados pelo backend
    private String classificacaoImc;   // "Peso normal", "Sobrepeso", etc.
    private String corImc;             // "green" | "yellow" | "orange" | "red" | "blue"
}