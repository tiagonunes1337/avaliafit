package org.example.avaliafit.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "cardapio_refeicao")
@NoArgsConstructor
@AllArgsConstructor
public class CardapioRefeicao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idRefeicao;

    @ManyToOne
    @JoinColumn(name = "idPlano", nullable = false)
    private PlanoNutricional planoNutricional;

    @Column(nullable = false)
    private String nomeRefeicao;

    private String descricao;
}
