package com.hrtrack.controllers;

import com.hrtrack.domain.dtos.MessageDto;
import com.hrtrack.domain.dtos.UserSimpleDto;
import com.hrtrack.domain.requests.CreateMessageRequest;
import com.hrtrack.domain.responses.message.*;
import com.hrtrack.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Contrôleur REST pour la gestion des messages
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * Récupère tous les messages de l'utilisateur connecté
     * GET /api/v1/messages/my-messages
     */
    @GetMapping("/my-messages")
    public ResponseEntity<MessageListResponse> getMyMessages(
            @RequestParam(required = false) Long senderId,
            @RequestParam(required = false) String date
    ) {
        try {
            LocalDate localDate = null;
            if (date != null && !date.trim().isEmpty()) {
                try {
                    localDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body(
                            MessageListResponse.builder()
                                    .success(false)
                                    .message("Format de date invalide. Utilisez le format yyyy-MM-dd")
                                    .messages(null)
                                    .build()
                    );
                }
            }

            List<MessageDto> messages = messageService.getMyMessages(senderId, localDate);
            return ResponseEntity.ok(
                    MessageListResponse.builder()
                            .success(true)
                            .message("Messages récupérés avec succès")
                            .messages(messages)
                            .build()
            );
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des messages: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    MessageListResponse.builder()
                            .success(false)
                            .message("Erreur lors de la récupération des messages: " + e.getMessage())
                            .messages(null)
                            .build()
            );
        }
    }

    /**
     * Récupère un message par son UUID
     * GET /api/v1/messages/{uuid}
     */
    @GetMapping("/{uuid}")
    public ResponseEntity<MessageResponse> getMessageByUuid(@PathVariable String uuid) {
        try {
            UUID messageUuid = UUID.fromString(uuid);
            MessageDto message = messageService.getMessageByUuid(messageUuid);
            return ResponseEntity.ok(
                    MessageResponse.builder()
                            .success(true)
                            .message("Message récupéré avec succès")
                            .data(message)
                            .build()
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    MessageResponse.builder()
                            .success(false)
                            .message("UUID invalide")
                            .data(null)
                            .build()
            );
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du message {}: {}", uuid, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    MessageResponse.builder()
                            .success(false)
                            .message("Erreur lors de la récupération du message: " + e.getMessage())
                            .data(null)
                            .build()
            );
        }
    }

    /**
     * Envoie un nouveau message
     * POST /api/v1/messages/send
     */
    @PostMapping("/send")
    public ResponseEntity<MessageResponse> sendMessage(@RequestBody CreateMessageRequest request) {
        try {
            MessageDto message = messageService.sendMessage(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    MessageResponse.builder()
                            .success(true)
                            .message("Message envoyé avec succès")
                            .data(message)
                            .build()
            );
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi du message: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    MessageResponse.builder()
                            .success(false)
                            .message("Erreur lors de l'envoi du message: " + e.getMessage())
                            .data(null)
                            .build()
            );
        }
    }

    /**
     * Marque un message comme lu
     * PUT /api/v1/messages/{uuid}/mark-as-read
     */
    @PutMapping("/{uuid}/mark-as-read")
    public ResponseEntity<MessageResponse> markAsRead(@PathVariable String uuid) {
        try {
            UUID messageUuid = UUID.fromString(uuid);
            MessageDto message = messageService.markAsRead(messageUuid);
            return ResponseEntity.ok(
                    MessageResponse.builder()
                            .success(true)
                            .message("Message marqué comme lu")
                            .data(message)
                            .build()
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    MessageResponse.builder()
                            .success(false)
                            .message("UUID invalide")
                            .data(null)
                            .build()
            );
        } catch (Exception e) {
            log.error("Erreur lors du marquage du message {} comme lu: {}", uuid, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    MessageResponse.builder()
                            .success(false)
                            .message("Erreur lors du marquage du message comme lu: " + e.getMessage())
                            .data(null)
                            .build()
            );
        }
    }

    /**
     * Supprime un message (soft delete)
     * DELETE /api/v1/messages/{uuid}
     */
    @DeleteMapping("/{uuid}")
    public ResponseEntity<MessageResponse> deleteMessage(@PathVariable String uuid) {
        try {
            UUID messageUuid = UUID.fromString(uuid);
            boolean deleted = messageService.deleteMessage(messageUuid);
            return ResponseEntity.ok(
                    MessageResponse.builder()
                            .success(deleted)
                            .message(deleted ? "Message supprimé avec succès" : "Échec de la suppression du message")
                            .data(null)
                            .build()
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    MessageResponse.builder()
                            .success(false)
                            .message("UUID invalide")
                            .data(null)
                            .build()
            );
        } catch (Exception e) {
            log.error("Erreur lors de la suppression du message {}: {}", uuid, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    MessageResponse.builder()
                            .success(false)
                            .message("Erreur lors de la suppression du message: " + e.getMessage())
                            .data(null)
                            .build()
            );
        }
    }

    /**
     * Récupère le nombre de messages non lus
     * GET /api/v1/messages/unread-count
     */
    @GetMapping("/unread-count")
    public ResponseEntity<UnreadCountResponse> getUnreadCount() {
        try {
            int count = messageService.getUnreadCount();
            return ResponseEntity.ok(
                    UnreadCountResponse.builder()
                            .success(true)
                            .message("Nombre de messages non lus récupéré avec succès")
                            .count(count)
                            .build()
            );
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du nombre de messages non lus: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    UnreadCountResponse.builder()
                            .success(false)
                            .message("Erreur lors de la récupération du nombre de messages non lus: " + e.getMessage())
                            .count(0)
                            .build()
            );
        }
    }

    /**
     * Récupère la liste des émetteurs qui ont envoyé des messages à l'utilisateur
     * GET /api/v1/messages/senders
     */
    @GetMapping("/senders")
    public ResponseEntity<SendersListResponse> getSenders() {
        try {
            List<UserSimpleDto> senders = messageService.getSenders();
            return ResponseEntity.ok(
                    SendersListResponse.builder()
                            .success(true)
                            .message("Liste des émetteurs récupérée avec succès")
                            .senders(senders)
                            .build()
            );
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des émetteurs: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    SendersListResponse.builder()
                            .success(false)
                            .message("Erreur lors de la récupération des émetteurs: " + e.getMessage())
                            .senders(null)
                            .build()
            );
        }
    }

    /**
     * Récupère la liste de tous les utilisateurs (pour composer un message)
     * GET /api/v1/messages/users
     */
    @GetMapping("/users")
    public ResponseEntity<UsersListResponse> getAllUsers() {
        try {
            List<UserSimpleDto> users = messageService.getAllUsers();
            return ResponseEntity.ok(
                    UsersListResponse.builder()
                            .success(true)
                            .message("Liste des utilisateurs récupérée avec succès")
                            .users(users)
                            .build()
            );
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des utilisateurs: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    UsersListResponse.builder()
                            .success(false)
                            .message("Erreur lors de la récupération des utilisateurs: " + e.getMessage())
                            .users(null)
                            .build()
            );
        }
    }
}