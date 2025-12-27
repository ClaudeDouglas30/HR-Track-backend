package com.hrtrack.service.implementation;

import com.hrtrack.domain.PointageStatus;
import com.hrtrack.domain.Role;
import com.hrtrack.domain.entities.Pointage;
import com.hrtrack.domain.entities.User;
import com.hrtrack.domain.requests.pointage.PointageRequest;
import com.hrtrack.domain.responses.pointage.PointageResponse;
import com.hrtrack.mappers.PointageMapper;
import com.hrtrack.repository.PointageRepository;
import com.hrtrack.repository.UserRepository;
import com.hrtrack.service.IPointageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PointageServiceImpl implements IPointageService {

    private final PointageRepository pointageRepository;
    private final UserRepository userRepository;
    private final PointageMapper pointageMapper;

    private static final LocalTime HEURE_LIMITE = LocalTime.of(8, 30);

    // Méthode pour calculer le statut
    private PointageStatus calculerStatus(LocalTime heureArrivee) {
        return heureArrivee.isAfter(HEURE_LIMITE) ? PointageStatus.RETARD : PointageStatus.PRESENT;
    }

    @Override
    public PointageResponse pointer(PointageRequest request, Long userId) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate today = LocalDate.now();
        java.sql.Date todaySql = java.sql.Date.valueOf(today);
        List<Pointage> pointagesDuJour = pointageRepository.findByUserIdAndDate(userId, todaySql);

        if (pointagesDuJour.size() >= 2) {
            throw new Exception("On ne peut faire que deux pointages par jour");
        }

        try {
            Pointage pointage = new Pointage();
            pointage.setDate(todaySql);
            pointage.setHeureArrivee(request.heureEntree());
            pointage.setHeureDepart(request.heureSortie());
            pointage.setStatut(calculerStatus(request.heureEntree()));
            pointage.setUser(user);
            return pointageMapper.toResponse(pointageRepository.save(pointage));
        } catch (Exception e) {
            throw new Exception("Une erreur est survenue lors du pointage");
        }

    }

    @Override
    public List<PointageResponse> getPointages(User user) {

        List<Pointage> pointages =
                user.getRole() == Role.RH
                        ? pointageRepository.findAll()
                        : pointageRepository.findByUserId(user.getId());

        return pointages.stream()
                .map(pointageMapper::toResponse)
                .toList();
    }
}