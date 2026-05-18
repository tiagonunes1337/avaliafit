package org.example.avaliafit.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "avaliacao")
@NoArgsConstructor
@AllArgsConstructor
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idAvaliacao;

    @ManyToOne
    @JoinColumn(name = "idPaciente", nullable = false)
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "idFuncionario", nullable = false)
    private Funcionario funcionario;

    @Column(nullable = false)
    private LocalDateTime dataAvaliacao;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal peso;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal altura;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal imc;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal percentualGordura;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal massaMuscular;

    private String observacoes;

    // NOVO: Relacionamento com Agendamento
    @OneToOne(fetch = FetchType.LAZY) // Uma avaliação está ligada a um agendamento
    @JoinColumn(name = "idAgendamento", unique = true) // A coluna no banco será idAgendamento e deve ser única
    private Agendamento agendamento;
}
