package org.example.avaliafit.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "plano_nutricional")
@NoArgsConstructor
@AllArgsConstructor
public class PlanoNutricional {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPlano;

    @ManyToOne
    @JoinColumn(name = "idPaciente", nullable = false)
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "idFuncionario", nullable = false)
    private Funcionario funcionario;

    @Column(nullable = false)
    private int kcalDiario;

    @Column(nullable = false)
    private int proteinas;

    @Column(nullable = false)
    private int carboidratos;

    @Column(name = "gorduras", nullable = false)
    private int gorduras;

    @Column(nullable = false)
    private LocalDate dataInicio;

    @Column(nullable = false)
    private boolean ativo = true;
}
