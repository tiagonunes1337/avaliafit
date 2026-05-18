package org.example.avaliafit.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "paciente")
@NoArgsConstructor
@AllArgsConstructor
public class Paciente {

    @Id
    private Integer idPaciente;

    @OneToOne
    @MapsId
    @JoinColumn(name = "idPaciente")
    private Usuario usuario;

    @Column(nullable = false)
    private String objetivo;
}