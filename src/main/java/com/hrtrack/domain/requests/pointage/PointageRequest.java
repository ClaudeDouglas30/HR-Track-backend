package com.hrtrack.domain.requests.pointage;

import java.time.LocalTime;

public record PointageRequest(
        LocalTime heureEntree,
        LocalTime heureSortie
) {}
