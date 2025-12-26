package com.hrtrack.mappers;

import com.hrtrack.domain.dtos.MessageDto;
import com.hrtrack.domain.dtos.UserSimpleDto;
import com.hrtrack.domain.entities.Message;
import com.hrtrack.domain.entities.User;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

/**
 * Mapper pour convertir entre Message (entité) et MessageDto
 */
@Component
public class MessageMapper {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Convertit une entité Message en MessageDto
     */
    public MessageDto toDto(Message message) {
        if (message == null) {
            return null;
        }

        return MessageDto.builder()
                .id(message.getId())
                .messageUuid(message.getMessageUuid().toString())
                .sender(toUserSimpleDto(message.getSender()))
                .receiver(toUserSimpleDto(message.getReceiver()))
                .subject(message.getSubject())
                .content(message.getContent())
                .dateEnvoi(message.getDateEnvoi() != null ? message.getDateEnvoi().format(ISO_FORMATTER) : null)
                .isRead(message.isRead())
                .createdAt(message.getCreatedAt() != null ? message.getCreatedAt().format(ISO_FORMATTER) : null)
                .updatedAt(message.getUpdatedAt() != null ? message.getUpdatedAt().format(ISO_FORMATTER) : null)
                .build();
    }

    /**
     * Convertit un User en UserSimpleDto
     */
    public UserSimpleDto toUserSimpleDto(User user) {
        if (user == null) {
            return null;
        }

        return UserSimpleDto.builder()
                .id(user.getId())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .email(user.getEmail())
                .build();
    }
}