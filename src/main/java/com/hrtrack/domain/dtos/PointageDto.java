package com.hrtrack.domain.dtos;

import com.hrtrack.domain.PointageStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.Date;

/**
 * DTO pour les pointages.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PointageDto {
    private Long id;
    private Date date;
    private LocalTime heureArrivee;
    private LocalTime heureDepart;
    private PointageStatus statut;
    private String jour; // Jour de la semaine (Lun, Mar, etc.)
}

