package org.example.avaliafit.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AvaliacaoRequestDTO {

        private Integer idPaciente;
        private Integer idFuncionario;
        private BigDecimal peso;
        private BigDecimal altura;
        private BigDecimal imc;
        private BigDecimal percentualGordura;
        private BigDecimal massaMuscular;
        private String observacoes;
        private Integer idAgendamento;
}
