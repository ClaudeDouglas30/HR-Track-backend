package com.hrtrack.domain.entities;

import com.hrtrack.domain.Libelle;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name="type_conges")
public class TypeConge {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "typeconge_seq")
    @SequenceGenerator(name="typeconge_seq",sequenceName = "typeconges_sequence",allocationSize = 100)
    private int id;

    @Enumerated(EnumType.STRING)
    private Libelle libelle;

    private int quotaAnnuel;

    private String description;

    @OneToMany(mappedBy = "typeConge")
    private List<Conge> conges = new ArrayList<>();
}
