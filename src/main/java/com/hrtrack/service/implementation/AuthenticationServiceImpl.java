package com.hrtrack.service.implementation;

import com.hrtrack.domain.Role;
import com.hrtrack.domain.dtos.UserDto;
import com.hrtrack.domain.entities.User;
import com.hrtrack.domain.requests.auth.AuthenticationRequest;
import com.hrtrack.domain.requests.auth.RegisterRequest;
import com.hrtrack.domain.responses.AuthenticationResponse;
import com.hrtrack.exception.EmailingException;
import com.hrtrack.mappers.UserMapper;
import com.hrtrack.repository.UserRepository;
import com.hrtrack.service.AuthenticationService;
import com.hrtrack.service.EmailService;
import com.hrtrack.service.JwtService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final UserMapper userMapper;


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
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        return AuthenticationResponse.builder()
                .success(true)
                .message("Connexion réussie.")
                .token(jwt)
                .user(userDto)
                .build();
    }
}
