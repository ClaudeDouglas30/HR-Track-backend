package com.hrtrack.service.implementation;

import com.hrtrack.domain.dtos.MessageDto;
import com.hrtrack.domain.dtos.UserSimpleDto;
import com.hrtrack.domain.entities.Message;
import com.hrtrack.domain.entities.User;
import com.hrtrack.domain.requests.CreateMessageRequest;
import com.hrtrack.exception.UserException;
import com.hrtrack.exception.ValidationException;
import com.hrtrack.mappers.MessageMapper;
import com.hrtrack.repository.MessageRepository;
import com.hrtrack.repository.UserRepository;
import com.hrtrack.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implémentation du service de gestion des messages
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final MessageMapper messageMapper;

    @Override
    @Transactional(readOnly = true)
    public List<MessageDto> getMyMessages(Long senderId, LocalDate date) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new UserException("Utilisateur non authentifié");
        }

        List<Message> messages;

        if (senderId != null && date != null) {
            // Filtrer par émetteur et date
            User sender = userRepository.findById(senderId)
                    .orElseThrow(() -> new UserException("Émetteur non trouvé"));
            messages = messageRepository.findByReceiverAndSenderAndDateAndNotDeleted(currentUser, sender, date);
        } else if (senderId != null) {
            // Filtrer par émetteur uniquement
            User sender = userRepository.findById(senderId)
                    .orElseThrow(() -> new UserException("Émetteur non trouvé"));
            messages = messageRepository.findByReceiverAndSenderAndNotDeleted(currentUser, sender);
        } else if (date != null) {
            // Filtrer par date uniquement
            messages = messageRepository.findByReceiverAndDateAndNotDeleted(currentUser, date);
        } else {
            // Récupérer tous les messages
            messages = messageRepository.findByReceiverAndNotDeleted(currentUser);
        }

        log.info("Récupération de {} message(s) pour l'utilisateur {}", messages.size(), currentUser.getEmail());
        return messages.stream()
                .map(messageMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MessageDto getMessageByUuid(UUID uuid) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new UserException("Utilisateur non authentifié");
        }

        Message message = messageRepository.findByMessageUuid(uuid)
                .orElseThrow(() -> new ValidationException("Message non trouvé"));

        // Vérifier que l'utilisateur est soit l'émetteur soit le destinataire
        if (!message.getSender().getId().equals(currentUser.getId()) &&
            !message.getReceiver().getId().equals(currentUser.getId())) {
            throw new UserException("Vous n'avez pas accès à ce message");
        }

        // Vérifier que le message n'est pas supprimé par l'utilisateur courant
        if (message.getReceiver().getId().equals(currentUser.getId()) && message.isDeletedByReceiver()) {
            throw new ValidationException("Message supprimé");
        }
        if (message.getSender().getId().equals(currentUser.getId()) && message.isDeletedBySender()) {
            throw new ValidationException("Message supprimé");
        }

        log.info("Message {} récupéré par l'utilisateur {}", uuid, currentUser.getEmail());
        return messageMapper.toDto(message);
    }

    @Override
    @Transactional
    public MessageDto sendMessage(CreateMessageRequest request) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new UserException("Utilisateur non authentifié");
        }

        // Validation des données
        if (request.getReceiverId() == null) {
            throw new ValidationException("Le destinataire est requis");
        }
        if (request.getSubject() == null || request.getSubject().trim().isEmpty()) {
            throw new ValidationException("Le sujet est requis");
        }
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new ValidationException("Le contenu est requis");
        }

        // Récupérer le destinataire
        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new UserException("Destinataire non trouvé"));

        // Vérifier que l'utilisateur n'envoie pas un message à lui-même
        if (receiver.getId().equals(currentUser.getId())) {
            throw new ValidationException("Vous ne pouvez pas vous envoyer un message à vous-même");
        }

        // Créer le message
        Message message = Message.builder()
                .messageUuid(UUID.randomUUID())
                .sender(currentUser)
                .receiver(receiver)
                .subject(request.getSubject().trim())
                .content(request.getContent().trim())
                .dateEnvoi(LocalDateTime.now())
                .isRead(false)
                .isDeletedBySender(false)
                .isDeletedByReceiver(false)
                .build();

        Message savedMessage = messageRepository.save(message);
        log.info("Message envoyé de {} à {} avec le sujet: {}",
                currentUser.getEmail(), receiver.getEmail(), request.getSubject());

        return messageMapper.toDto(savedMessage);
    }

    @Override
    @Transactional
    public MessageDto markAsRead(UUID uuid) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new UserException("Utilisateur non authentifié");
        }

        Message message = messageRepository.findByMessageUuid(uuid)
                .orElseThrow(() -> new ValidationException("Message non trouvé"));

        // Vérifier que l'utilisateur est le destinataire
        if (!message.getReceiver().getId().equals(currentUser.getId())) {
            throw new UserException("Vous n'êtes pas le destinataire de ce message");
        }

        // Vérifier que le message n'est pas supprimé
        if (message.isDeletedByReceiver()) {
            throw new ValidationException("Message supprimé");
        }

        // Marquer comme lu
        message.setRead(true);
        Message savedMessage = messageRepository.save(message);

        log.info("Message {} marqué comme lu par {}", uuid, currentUser.getEmail());
        return messageMapper.toDto(savedMessage);
    }

    @Override
    @Transactional
    public boolean deleteMessage(UUID uuid) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new UserException("Utilisateur non authentifié");
        }

        Message message = messageRepository.findByMessageUuid(uuid)
                .orElseThrow(() -> new ValidationException("Message non trouvé"));

        // Vérifier que l'utilisateur est soit l'émetteur soit le destinataire
        boolean isSender = message.getSender().getId().equals(currentUser.getId());
        boolean isReceiver = message.getReceiver().getId().equals(currentUser.getId());

        if (!isSender && !isReceiver) {
            throw new UserException("Vous n'avez pas accès à ce message");
        }

        // Soft delete selon le rôle de l'utilisateur
        if (isSender) {
            message.setDeletedBySender(true);
        }
        if (isReceiver) {
            message.setDeletedByReceiver(true);
        }

        // Si le message est supprimé par les deux parties, le supprimer définitivement
        if (message.isDeletedBySender() && message.isDeletedByReceiver()) {
            messageRepository.delete(message);
            log.info("Message {} supprimé définitivement", uuid);
        } else {
            messageRepository.save(message);
            log.info("Message {} marqué comme supprimé par {}", uuid, currentUser.getEmail());
        }

        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public int getUnreadCount() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new UserException("Utilisateur non authentifié");
        }

        int count = messageRepository.countUnreadByReceiver(currentUser);
        log.info("Utilisateur {} a {} message(s) non lu(s)", currentUser.getEmail(), count);
        return count;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSimpleDto> getSenders() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new UserException("Utilisateur non authentifié");
        }

        List<User> senders = messageRepository.findDistinctSendersByReceiver(currentUser);
        log.info("Récupération de {} émetteur(s) pour l'utilisateur {}", senders.size(), currentUser.getEmail());

        return senders.stream()
                .map(messageMapper::toUserSimpleDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSimpleDto> getAllUsers() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new UserException("Utilisateur non authentifié");
        }

        // Récupérer tous les utilisateurs sauf l'utilisateur courant
        List<User> users = userRepository.findAll().stream()
                .filter(user -> !user.getId().equals(currentUser.getId()))
                .collect(Collectors.toList());

        log.info("Récupération de {} utilisateur(s) pour composer un message", users.size());

        return users.stream()
                .map(messageMapper::toUserSimpleDto)
                .collect(Collectors.toList());
    }

    /**
     * Récupère l'utilisateur actuellement authentifié
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String email;
        if (authentication.getPrincipal() instanceof UserDetails) {
            email = ((UserDetails) authentication.getPrincipal()).getUsername();
        } else {
            email = authentication.getPrincipal().toString();
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException("Utilisateur non trouvé"));
    }
}