package com.hrtrack.domain.entities;

import com.hrtrack.domain.Statut;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;



@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name="conges")
public class Conge {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "conge_seq")
    @SequenceGenerator(name="conge_seq",sequenceName = "conges_sequence",allocationSize = 100)
    private int id;

    private LocalDate dateDebut;
    private LocalDate dateFin;

    @Enumerated(EnumType.STRING)
    @Column(length = 20,nullable = false)
    private Statut statut;

    @Column(nullable = false)
    private LocalDateTime dateSoumission;

    @Column(columnDefinition = "TEXT")
    private String commentaireEmployee;

    @Column(nullable = false)
    private String justificatif;
    private String annotationRH;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "typeconge_id")
    private TypeConge typeConge;
}
