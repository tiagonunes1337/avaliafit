
package org.example.avaliafit.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO de Resposta para Auditoria de Avaliação
 *
 * Usado quando o frontend solicita o histórico de alterações de uma avaliação
 * Exemplo: GET /avaliacoes/1/auditoria
 *
 * Enriquecido com contexto (nome do funcionário, cargo) para melhor visualização
 */
@Data
public class AuditoriaAvaliacaoResponseDTO {

    private Integer idAuditoria;
    private Integer idAvaliacao;
    private String nomeFuncionario;
    private String cargoFuncionario;
    private String campoAlterado;      // "peso", "altura", "percentualGordura", etc.
    private String valorAnterior;      // "70.00"
    private String valorNovo;          // "72.00"
    private LocalDateTime dataAlteracao;
    private String motivo;             // "Erro de digitação", "Medição incorreta", etc.
}