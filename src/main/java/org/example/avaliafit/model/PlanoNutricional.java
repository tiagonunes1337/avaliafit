package org.example.avaliafit.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
    private Double kcalDiario;

    @Column(nullable = false)
    private Double proteinas;

    @Column(nullable = false)
    private Double carboidratos;

    @Column(name = "gorduras", nullable = false)
    private Double gorduras;

    @Column(nullable = false)
    private LocalDate dataInicio;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(nullable = false)
    private Double pesoObjetivo; // Ex: 70.0

    @Column(nullable = false)
    private Double metaAguaLitros; // Ex: 2.0 ou 2.5

    @OneToMany(mappedBy = "planoNutricional", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CardapioRefeicao> refeicoes = new ArrayList<>();
}