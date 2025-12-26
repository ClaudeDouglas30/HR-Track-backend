package com.hrtrack.mappers;

import com.hrtrack.domain.dtos.UserDto;
import com.hrtrack.domain.entities.User;
import com.hrtrack.utils.FileUrlUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final FileUrlUtil fileUrlUtil;

    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        UserDto userDto = new UserDto();
        userDto.setId(user.getId() != null ? user.getId().intValue() : 0);
        userDto.setNom(safeString(user.getNom()));
        userDto.setPrenom(safeString(user.getPrenom()));
        userDto.setEmail(safeString(user.getEmail()));
        userDto.setTelephone(safeString(user.getTelephone()));
        userDto.setRole(user.getRole());
        userDto.setLastLogin(user.getLastLogin());
        userDto.setCreatedAt(user.getCreatedAt());
        userDto.setUpdatedAt(user.getUpdatedAt());

        // Convert photo path to URL
        if (user.getPhotoProfil() != null && !user.getPhotoProfil().isEmpty()) {
            userDto.setPhotoProfil(fileUrlUtil.convertToUrl(user.getPhotoProfil()));
        }

        return userDto;
    }

    public User toEntity(UserDto userDto) {
        if (userDto == null) {
            return null;
        }

        User user = new User();
        user.setId(userDto.getId() != 0 ? Long.valueOf(userDto.getId()) : null);
        user.setNom(userDto.getNom());
        user.setPrenom(userDto.getPrenom());
        user.setEmail(userDto.getEmail());
        user.setTelephone(userDto.getTelephone());
        user.setPhotoProfil(userDto.getPhotoProfil());
        user.setRole(userDto.getRole());
        user.setLastLogin(userDto.getLastLogin());
        user.setCreatedAt(userDto.getCreatedAt());
        user.setUpdatedAt(userDto.getUpdatedAt());

        return user;
    }

    /**
     * Garantit qu'une chaîne n'est jamais null (retourne "" si null)
     */
    private String safeString(String value) {
        return value != null ? value : "";
    }
}