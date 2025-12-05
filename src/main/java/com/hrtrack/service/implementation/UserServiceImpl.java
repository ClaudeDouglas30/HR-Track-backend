package com.hrtrack.service.implementation;

import com.hrtrack.domain.entities.User;
import com.hrtrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Méthode requise par l'interface UserDetailsService.
     * Elle est utilisée par le AuthenticationManager lors de l'authentification.
     * @param username Le nom d'utilisateur (ici l'email) fourni lors de la connexion.
     * @return Les détails de l'utilisateur (votre objet User).
     * @throws UsernameNotFoundException Si aucun utilisateur n'est trouvé.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'email: " + username));
    }
}
