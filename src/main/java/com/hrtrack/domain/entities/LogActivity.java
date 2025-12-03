package com.hrtrack.domain.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name="log_activities")
public class LogActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "log_seq")
    @SequenceGenerator(name="log_seq",sequenceName = "logs_sequence",allocationSize = 10000)
    private Long id;

    private String action;
    private LocalDateTime dateAction;

    @Column(columnDefinition = "TEXT")
    private String details;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

}
