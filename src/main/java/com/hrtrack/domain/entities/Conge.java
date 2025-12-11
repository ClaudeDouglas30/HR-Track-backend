package com.hrtrack.domain.entities;

import com.hrtrack.domain.CongeStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name="conges", indexes = {
        @Index(name = "idx_congeuuid_conges", columnList = "conge_uuid", unique = true),
        @Index(name = "idx_user_conge", columnList = "user_id")
})
public class Conge {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "conge_seq")
    @SequenceGenerator(name="conge_seq",sequenceName = "conges_sequence",allocationSize = 100)
    private Long id;

    @Column(unique = true)
    private UUID congeUuid =  UUID.randomUUID();

    private LocalDate dateDebut;
    private LocalDate dateFin;

    @Enumerated(EnumType.STRING)
    @Column(length = 20,nullable = false)
    private CongeStatus congeStatus = CongeStatus.ENATTENTE;

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

    @OneToMany(mappedBy = "conge",cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Justificatif> justificatifs;
}
