package com.hrtrack.service;

import com.hrtrack.domain.entities.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


public interface Userser {



    User updateUserProfile(int id, String nom, String prenom, String email, String telephone, MultipartFile profilePicture);

    void updateUserPassword(int id,String currentPassword, String newPassword);

    List<User> getAllUsers();

    List<User> getUsersByRole(String role);

    User getCurrentUser();
}