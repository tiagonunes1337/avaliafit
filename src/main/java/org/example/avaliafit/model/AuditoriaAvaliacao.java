package org.example.avaliafit.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "auditoria_avaliacao")
@NoArgsConstructor
@AllArgsConstructor

public class AuditoriaAvaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idAuditoria;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idAvaliacao", nullable = false)
    private Avaliacao avaliacao;;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idFuncionarioAlterou", nullable = false)
    private Funcionario funcionarioQueAlterou;


    @Column(nullable = false, length = 100)
    private String campoAlterado;

    @Column(nullable = false, length = 255)
    private String valorAnterior;

    @Column(nullable = false, length = 255)
    private String valorNovo;

    @Column(nullable = false)
    private LocalDateTime dataAlteracao = LocalDateTime.now();

    @Column(length = 500)
    private String motivo;
}
