package com.hrtrack.domain.responses.pointage;

import com.hrtrack.domain.PointageStatus;

import java.time.LocalTime;

public record PointageResponse(
        Long id,
        java.util.Date date,
        LocalTime heureEntree,
        LocalTime heureSortie,
        PointageStatus status,
        Long userId,
        String username,
        String fullName
) {}

