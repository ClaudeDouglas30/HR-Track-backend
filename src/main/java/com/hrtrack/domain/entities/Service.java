package com.hrtrack.domain.entities;


import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table
public class Service {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "service_seq")
    @SequenceGenerator(name="service_seq",sequenceName = "service_sequence",allocationSize = 100)
    private int id;

    @Column(nullable = false)
    private String nom;

    @ManyToOne
    @JoinColumn(name="departement_id")
    private Departement departement;
}
