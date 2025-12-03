package com.hrtrack.domain.entities;

import com.hrtrack.domain.PointageStatut;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.util.Date;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name="pointages")
public class Pointage {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "pointage_seq")
    @SequenceGenerator(name="pointage_seq",sequenceName = "pointages_sequence",allocationSize = 100)
    private Long id;
    private Date date;

    @Column(nullable = true)
    private LocalTime heureArrivee;

    @Column(nullable = true)
    private LocalTime heureDepart;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private PointageStatut statut;

    @ManyToOne
    @JoinColumn(name = "horaire_travail_id")
    private HoraireTravail horaireTravail;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

}
