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
    private String nomeRefeicao; // Ex: "Café da Manhã", "Almoço"

    @Column(columnDefinition = "TEXT")
    private String descricao; // Ex: "2 ovos mexidos, 1 fatia de pão integral, 1 xícara de café"

    // --- NOVOS CAMPOS PARA O CÁLCULO MATEMÁTICO ---

    @Column(nullable = false)
    private Double calorias; // Ex: 350.5

    @Column(nullable = false)
    private Double proteinas; // Em gramas. Ex: 25.0

    @Column(nullable = false)
    private Double carboidratos; // Em gramas. Ex: 30.0

    @Column(nullable = false)
    private Double gorduras; // Em gramas. Ex: 12.0
}