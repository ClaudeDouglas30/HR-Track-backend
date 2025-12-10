package com.hrtrack.service.implementation;

import com.hrtrack.domain.Role;
import com.hrtrack.domain.dtos.UserDto;
import com.hrtrack.domain.entities.JwtToken;
import com.hrtrack.domain.entities.User;
import com.hrtrack.domain.requests.auth.AuthenticationRequest;
import com.hrtrack.domain.requests.auth.RegisterRequest;
import com.hrtrack.domain.responses.AuthenticationResponse;
import com.hrtrack.domain.responses.RefreshTokenResponse;
import com.hrtrack.exception.EmailingException;
import com.hrtrack.exception.InvalidTokenException;
import com.hrtrack.mappers.UserMapper;
import com.hrtrack.repository.JwtTokenRepository;
import com.hrtrack.repository.UserRepository;
import com.hrtrack.service.AuthenticationService;
import com.hrtrack.service.EmailService;
import com.hrtrack.service.JwtService;
import com.hrtrack.service.JwtTokenService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.*;


@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final JwtTokenService  jwtTokenService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final UserMapper userMapper;
    private final JwtTokenRepository jwtTokenRepository;

    @Value("${application.frontend.url}")
    private String frontendUrl;


    @Override
    public User register(RegisterRequest request) throws MessagingException, EmailingException {

        var user = User.builder()
                .nom(request.nom().toUpperCase())
                .prenom(request.prenom())
                .email(request.email())
                .telephone(request.telephone())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.EMPLOYEE)
                .isAccountValidated(false)
                .build();

        Context context = new Context();
        context.setVariable("subject", "Votre compte a été créé avec succès - HR Track");
        context.setVariable("name", user.getNomComplet());


        userRepository.save(user);
        emailService.sendMail(
                user.getEmail(),
                "Votre compte a été créé avec succès - HR Track",
                "user-registration",
                context
        );

        return user;
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        var user = userRepository.findByEmail(request.email()).orElseThrow();

        UserDto userDto = userMapper.toDto(user);
        final String jwt = jwtService.generateToken(user);
        final String refreshToken = jwtService.generateRefreshToken(user);
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        jwtTokenService.revokeAllUserTokens(user);
        jwtTokenService.saveUserToken(user, jwt, refreshToken);

        return AuthenticationResponse.builder()
                .success(true)
                .message("Connexion réussie.")
                .token(jwt)
                .refreshToken(refreshToken)
                .user(userDto)
                .build();
    }

    @Override
    public void createPasswordResetTokenForUser(String email) throws MessagingException, EmailingException {

        String token = UUID.randomUUID().toString();

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.MINUTE, 30);
        Date expiryDate = cal.getTime();

        User user = userRepository.findByEmail(email).orElseThrow();
        user.setResetPasswordToken(token);
        user.setResetPasswordExpiration(expiryDate);
        userRepository.save(user);

        Context context = new Context();
        context.setVariable("token", token);
        context.setVariable("subject", "Vous avez demandé à réinitialiser votre mot de passe - HR Track");
        context.setVariable("resetUrl", STR."\{frontendUrl}/resetpassword?token=");

        emailService.sendMail(
                user.getEmail(),
                "Vous avez demandé à réinitialiser votre mot de passe - HR Track",
                "forgot-password",
                context
        );
    }

    @Override
    public void resetPasswordForUser(String token, String password) throws MessagingException, EmailingException {

        // Vérifier si le token existe et récupérer l'utilisateur associé
        User user = userRepository.findByResetPasswordToken(token);
        
        if (user == null) {
            throw new InvalidTokenException("Token de réinitialisation invalide ou introuvable.");
        }

        // Vérifier si le token n'est pas expiré
        Date now = new Date();
        if (user.getResetPasswordExpiration() == null || user.getResetPasswordExpiration().before(now)) {
            user.setResetPasswordToken(null);
            user.setResetPasswordExpiration(null);
            userRepository.save(user);
            throw new InvalidTokenException("Le token de réinitialisation a expiré. Veuillez en demander un nouveau.");
        }

        
        user.setPassword(passwordEncoder.encode(password));
        user.setResetPasswordToken(null);
        user.setResetPasswordExpiration(null);
        
        userRepository.save(user);

        // Envoyer un email de notification de changement de mot de passe
        Context context = new Context();
        context.setVariable("subject", "Votre mot de passe a été modifié - HR Track");
        context.setVariable("name", user.getNomComplet());

        emailService.sendMail(
                user.getEmail(),
                "Votre mot de passe a été modifié - HR Track",
                "reset-password",
                context
        );
    }

    @Override
    public RefreshTokenResponse refreshToken(String token) {
        var userEmail = jwtService.extractUsername(token);
        if (userEmail != null) {
            var user = this.userRepository.findByEmail(userEmail)
                    .orElseThrow();

            if (jwtTokenService.isRefreshTokenValid(token)) {
                var newToken = jwtService.generateToken(user);
                var newRefreshToken = jwtService.generateRefreshToken(user);
                jwtTokenService.rotateUserTokens(user, newToken, newRefreshToken, token);

                return RefreshTokenResponse.builder()
                        .success(true)
                        .message("Token renouvelé avec succès")
                        .token(newToken)
                        .refreshToken(newRefreshToken)
                        .build();
            }
        }
        return RefreshTokenResponse.builder()
                .success(false)
                .message("Token invalide.")
                .token(null)
                .refreshToken(null)
                .build();
    }


}
