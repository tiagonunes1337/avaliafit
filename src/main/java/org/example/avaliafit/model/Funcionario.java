package org.example.avaliafit.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "funcionario")
@NoArgsConstructor
@AllArgsConstructor
public class Funcionario {

    @Id
    private Integer idFuncionario;

    @OneToOne
    @MapsId
    @JoinColumn(name = "idFuncionario")
    private Usuario usuario;

    @Column(length = 100, nullable = false)
    private String cargo;

}
