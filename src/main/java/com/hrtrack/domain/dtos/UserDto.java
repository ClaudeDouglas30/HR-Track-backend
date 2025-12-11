package com.hrtrack.domain.dtos;

import com.hrtrack.domain.Role;
import lombok.*;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {

    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String photoProfil;
    private String telephone;
    private Role role;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
