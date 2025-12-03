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
@Table(name="type_conge")
public class TypeConge {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "type_conge_seq")
    @SequenceGenerator(name="type_conge_seq",sequenceName = "type_conges_sequence",allocationSize = 100)
    private int id;
    private Libelle libelle;
    private int quotaAnnuel;

    @OneToMany(mappedBy = "type_conge")
    private List<Conge> conges = new ArrayList<>();
}
