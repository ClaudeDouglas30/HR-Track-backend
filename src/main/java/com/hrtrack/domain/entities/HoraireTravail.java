package com.hrtrack.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name="horaire_travail")
public class HoraireTravail {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private String jour;
    private LocalTime heureDebut;
    private LocalTime heureFin;

    @OneToMany(mappedBy = "horaireTravail")
    private List<Pointage> pointages = new ArrayList<>();
}
