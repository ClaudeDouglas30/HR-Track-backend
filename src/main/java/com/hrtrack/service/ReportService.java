package com.hrtrack.service;

import java.io.ByteArrayOutputStream;
import java.time.YearMonth;

/**
 * Service pour la génération de rapports.
 */
public interface ReportService {

    /**
     * Génère un rapport Excel des absences et retards pour un mois donné.
     * 
     * @param yearMonth Le mois pour lequel générer le rapport (format: YYYY-MM)
     * @return ByteArrayOutputStream contenant le fichier Excel
     */
    ByteArrayOutputStream generateAttendanceReport(YearMonth yearMonth);
}

