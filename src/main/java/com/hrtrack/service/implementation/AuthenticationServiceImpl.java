package com.hrtrack.service.implementation;

import com.hrtrack.domain.Role;
import com.hrtrack.domain.dtos.UserDto;
import com.hrtrack.domain.entities.User;
import com.hrtrack.domain.requests.auth.AuthenticationRequest;
import com.hrtrack.domain.requests.auth.RegisterRequest;
import com.hrtrack.domain.responses.auth.AuthenticationResponse;
import com.hrtrack.domain.responses.auth.RefreshTokenResponse;
import com.hrtrack.exception.EmailingException;
import com.hrtrack.exception.InvalidTokenException;
import com.hrtrack.mappers.UserMapper;
import com.hrtrack.repository.JwtTokenRepository;
import com.hrtrack.repository.UserRepository;
import com.hrtrack.service.AuthenticationService;
import com.hrtrack.service.EmailService;
import com.hrtrack.service.JwtService;
import com.hrtrack.service.JwtTokenService;
import com.hrtrack.utils.TokenGenerator;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.*;


@Slf4j
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

    @Value("${application.frontend.web.url}")
    private String frontendWebUrl;

    @Value("${application.frontend.mobile.url}")
    private String frontendMobileUrl;


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

        return new AuthenticationResponse(
                true,
                "Connexion réussie.",
                jwt,
                refreshToken,
                userDto
        );
    }

    @Override
    public void createPasswordResetTokenForUser(String email, String platform) throws MessagingException, EmailingException {

        // Générer un token sécurisé de 32 caractères
        String token = TokenGenerator.generateSecureToken();

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.MINUTE, 30);
        Date expiryDate = cal.getTime();

        User user = userRepository.findByEmail(email).orElseThrow();
        user.setResetPasswordToken(token);
        user.setResetPasswordExpiration(expiryDate);
        userRepository.save(user);

        // Normaliser la plateforme pour éviter les problèmes (trim, uppercase, null-safe)
        String normalizedPlatform = platform != null ? platform.trim().toUpperCase() : "WEB";
        log.info("Demande de réinitialisation de mot de passe pour l'email : {}, plateforme reçue : '{}' (normalisée : '{}')", 
                email, platform != null ? platform : "null", normalizedPlatform);

        // Déterminer l'URL complète de réinitialisation selon la plateforme
        String resetUrl = determineResetUrl(normalizedPlatform, token);

        Context context = new Context();
        context.setVariable("token", token);
        context.setVariable("subject", "Vous avez demandé à réinitialiser votre mot de passe - HR Track");
        context.setVariable("resetUrl", resetUrl);

        emailService.sendMail(
                user.getEmail(),
                "Vous avez demandé à réinitialiser votre mot de passe - HR Track",
                "forgot-password",
                context
        );
    }

    /**
     * Détermine l'URL complète de réinitialisation selon la plateforme.
     * 
     * @param platform La plateforme (WEB ou MOBILE)
     * @param token Le token de réinitialisation
     * @return L'URL complète de réinitialisation avec le token
     */
    private String determineResetUrl(String platform, String token) {
        String baseUrl;
        String resetPath;
        
        // Normaliser la plateforme (trim et uppercase pour éviter les problèmes de casse)
        String normalizedPlatform = platform != null ? platform.trim().toUpperCase() : "WEB";
        
        if ("MOBILE".equals(normalizedPlatform)) {
            baseUrl = frontendMobileUrl;
            resetPath = "/reset-password";
            log.info("Génération du lien de réinitialisation pour MOBILE : {}{}?token={}", baseUrl, resetPath, token);
        } else {
            baseUrl = frontendWebUrl;
            resetPath = "/administration/auth/reset-password";
            log.info("Génération du lien de réinitialisation pour WEB : {}{}?token={}", baseUrl, resetPath, token);
        }
        
        String finalUrl = baseUrl + resetPath + "?token=" + token;
        log.debug("URL de réinitialisation générée : {}", finalUrl);
        
        return finalUrl;
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

                return new RefreshTokenResponse(
                        true,
                        "Token renouvelé avec succès",
                        newToken,
                        newRefreshToken
                );
            }
        }
        return new RefreshTokenResponse(
                false,
                "Token invalide.",
                null,
                null
        );
    }


}