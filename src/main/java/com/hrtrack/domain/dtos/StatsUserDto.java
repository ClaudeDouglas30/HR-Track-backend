package com.hrtrack.domain.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StatsUserDto {
    private int totalCongeAccepte;
    private double tauxPresence;
    private int joursPointes;
    private int messagesEnvoyes;
    private int messagesRecus;
    private double heuresEffectueesMois;
}