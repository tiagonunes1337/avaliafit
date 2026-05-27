package org.example.avaliafit.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;


@Data
public class AvaliacaoRequestDTO {

        @NotNull(message = "O identificador do agendamento é obrigatório")
        private Integer idAgendamento;

        private Integer idPaciente;
        private Integer idFuncionario;

        @NotNull(message = "O peso é obrigatório")
        private BigDecimal peso;

        @NotNull(message = "A altura é obrigatória")
        private BigDecimal altura;

        private BigDecimal imc;
        private BigDecimal percentualGordura;
        private BigDecimal massaMuscular;
        private String observacoes;
}
