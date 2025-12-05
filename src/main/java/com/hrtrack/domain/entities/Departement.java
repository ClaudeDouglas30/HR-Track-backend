package com.hrtrack.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "departements")
public class Departement {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "depart_seq")
    @SequenceGenerator(name="depart_seq",sequenceName = "depart_sequence",allocationSize = 100)
    private int id;

    @Column(nullable = false)
    private String nom;

    private String description;

    @OneToMany(mappedBy = "departement")
    private List<Service> services = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "departement_users",
            joinColumns = @JoinColumn(name = "departement_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> users = new ArrayList<>();
}
