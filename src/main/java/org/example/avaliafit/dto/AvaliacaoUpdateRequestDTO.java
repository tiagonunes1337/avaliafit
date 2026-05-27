package org.example.avaliafit.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AvaliacaoUpdateRequestDTO {

    private BigDecimal peso;
    private BigDecimal altura;
    private BigDecimal percentualGordura;
    private BigDecimal massaMuscular;
    private String observacoes;

    @NotBlank(message = "O motivo da alteração é obrigatório")
    private String motivo;
}
