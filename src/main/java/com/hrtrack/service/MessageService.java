package com.hrtrack.service;

import com.hrtrack.domain.dtos.MessageDto;
import com.hrtrack.domain.dtos.UserSimpleDto;
import com.hrtrack.domain.requests.CreateMessageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Interface du service de gestion des messages
 */
public interface MessageService {

    /**
     * Récupère tous les messages de l'utilisateur connecté
     */
    List<MessageDto> getMyMessages(Long senderId, LocalDate date);

    /**
     * Récupère un message par son UUID
     */
    MessageDto getMessageByUuid(UUID uuid);

    /**
     * Envoie un nouveau message
     */
    MessageDto sendMessage(CreateMessageRequest request);

    /**
     * Marque un message comme lu
     */
    MessageDto markAsRead(UUID uuid);

    /**
     * Supprime un message (soft delete)
     */
    boolean deleteMessage(UUID uuid);

    /**
     * Récupère le nombre de messages non lus
     */
    int getUnreadCount();

    /**
     * Récupère la liste des émetteurs
     */
    List<UserSimpleDto> getSenders();

    /**
     * Récupère la liste de tous les utilisateurs
     */
    List<UserSimpleDto> getAllUsers();
}