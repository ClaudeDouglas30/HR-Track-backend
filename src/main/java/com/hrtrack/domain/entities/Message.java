package com.hrtrack.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité représentant un message entre utilisateurs
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "messages", indexes = {
        @Index(name = "idx_message_uuid", columnList = "messageUuid", unique = true),
        @Index(name = "idx_message_sender", columnList = "sender_id"),
        @Index(name = "idx_message_receiver", columnList = "receiver_id"),
        @Index(name = "idx_message_date", columnList = "dateEnvoi"),
        @Index(name = "idx_message_read", columnList = "isRead")
})
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "message_seq")
    @SequenceGenerator(name = "message_seq", sequenceName = "messages_sequence", allocationSize = 100)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID messageUuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(nullable = false, length = 255)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private LocalDateTime dateEnvoi;

    @Column(nullable = false)
    private boolean isRead = false;

    @Column(nullable = false)
    private boolean isDeletedBySender = false;

    @Column(nullable = false)
    private boolean isDeletedByReceiver = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}