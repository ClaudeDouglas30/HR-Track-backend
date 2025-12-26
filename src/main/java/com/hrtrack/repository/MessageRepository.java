package com.hrtrack.repository;

import com.hrtrack.domain.entities.Message;
import com.hrtrack.domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour la gestion des messages
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Trouve un message par son UUID
     */
    Optional<Message> findByMessageUuid(UUID messageUuid);

    /**
     * Récupère tous les messages reçus par un utilisateur (non supprimés par le destinataire)
     */
    @Query("SELECT m FROM Message m WHERE m.receiver = :receiver AND m.isDeletedByReceiver = false ORDER BY m.dateEnvoi DESC")
    List<Message> findByReceiverAndNotDeleted(@Param("receiver") User receiver);

    /**
     * Récupère les messages reçus par un utilisateur, filtrés par émetteur
     */
    @Query("SELECT m FROM Message m WHERE m.receiver = :receiver AND m.sender = :sender AND m.isDeletedByReceiver = false ORDER BY m.dateEnvoi DESC")
    List<Message> findByReceiverAndSenderAndNotDeleted(@Param("receiver") User receiver, @Param("sender") User sender);

    /**
     * Récupère les messages reçus par un utilisateur, filtrés par date
     */
    @Query("SELECT m FROM Message m WHERE m.receiver = :receiver AND DATE(m.dateEnvoi) = :date AND m.isDeletedByReceiver = false ORDER BY m.dateEnvoi DESC")
    List<Message> findByReceiverAndDateAndNotDeleted(@Param("receiver") User receiver, @Param("date") LocalDate date);

    /**
     * Récupère les messages reçus par un utilisateur, filtrés par émetteur et date
     */
    @Query("SELECT m FROM Message m WHERE m.receiver = :receiver AND m.sender = :sender AND DATE(m.dateEnvoi) = :date AND m.isDeletedByReceiver = false ORDER BY m.dateEnvoi DESC")
    List<Message> findByReceiverAndSenderAndDateAndNotDeleted(
            @Param("receiver") User receiver,
            @Param("sender") User sender,
            @Param("date") LocalDate date
    );

    /**
     * Compte le nombre de messages non lus pour un utilisateur
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiver = :receiver AND m.isRead = false AND m.isDeletedByReceiver = false")
    int countUnreadByReceiver(@Param("receiver") User receiver);

    /**
     * Récupère la liste des émetteurs uniques qui ont envoyé des messages à un utilisateur
     */
    @Query("SELECT DISTINCT m.sender FROM Message m WHERE m.receiver = :receiver AND m.isDeletedByReceiver = false ORDER BY m.sender.nom, m.sender.prenom")
    List<User> findDistinctSendersByReceiver(@Param("receiver") User receiver);
}