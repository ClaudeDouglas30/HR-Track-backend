package com.hrtrack.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "justificatifs", indexes = {
        @Index(name = "idx_justificatif_uuid", columnList = "nomFichier", unique = true)
})
public class Justificatif {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID nomFichier =  UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY)
    private Conge conge;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
