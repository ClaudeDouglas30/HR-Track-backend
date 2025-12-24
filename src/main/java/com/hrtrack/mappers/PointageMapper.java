package com.hrtrack.mappers;


import com.hrtrack.domain.entities.Pointage;
import com.hrtrack.domain.responses.pointage.PointageResponse;
import org.springframework.stereotype.Component;

@Component
public class PointageMapper {

    public PointageResponse toResponse(Pointage pointage) {


        if (pointage == null) {
            return null;
        }
        String fullName = pointage.getUser().getPrenom() + " " + pointage.getUser().getNom();
        return new PointageResponse(
                pointage.getId(),
                pointage.getDate(),
                pointage.getHeureArrivee(),
                pointage.getHeureDepart(),
                pointage.getStatut(),
                pointage.getUser().getId(),
                pointage.getUser().getUsername(),
                fullName

        );
    }
}