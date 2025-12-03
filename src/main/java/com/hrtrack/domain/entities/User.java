package com.hrtrack.domain.entities;

import com.hrtrack.domain.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name="users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "user_seq")
    @SequenceGenerator(name="user_seq",sequenceName = "users_sequence",allocationSize = 100)
    private int id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(unique = true,nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;
    private String photoProfil;
    private String telephone;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Role role;

    @OneToMany(mappedBy = "users",cascade = CascadeType.ALL)
    private List<Pointage> pointage = new ArrayList<>();

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user")
    private List<LogActivity> logActivities = new ArrayList<>();

    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL)
    private List<Conge> conges = new ArrayList<>();


    @ManyToMany
    @JoinColumn(name="users")
    private List<Departement> departements = new ArrayList<>();

}
