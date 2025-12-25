package com.hrtrack.controllers;

import com.hrtrack.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.YearMonth;

/**
 * Contrôleur pour la génération et le téléchargement de rapports.
 * 
 * Endpoints disponibles :
 * - GET /api/v1/reports/attendance : Télécharge un rapport Excel des absences et retards
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * Télécharge un rapport Excel des absences et retards pour un mois donné.
     * Accessible par ADMIN et RH.
     * 
     * @param month Mois pour lequel générer le rapport (format: YYYY-MM, ex: 2024-01)
     * @return Fichier Excel à télécharger
     */
    @GetMapping("/attendance")
    public ResponseEntity<byte[]> downloadAttendanceReport(
            @RequestParam(required = false) String month) {
        try {
            YearMonth yearMonth;
            // Si aucun mois n'est fourni, utiliser le mois actuel
            if (month == null || month.isEmpty()) {
                yearMonth = YearMonth.now();
            } else {
                // Parser le format YYYY-MM
                yearMonth = YearMonth.parse(month);
            }

            // Générer le rapport
            ByteArrayOutputStream outputStream = reportService.generateAttendanceReport(yearMonth);

            // Préparer les headers pour le téléchargement
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData(
                    "attachment",
                    "rapport_absences_retards_" + yearMonth + ".xlsx"
            );

            log.info("Rapport Excel téléchargé pour le mois: {}", yearMonth);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());

        } catch (Exception e) {
            log.error("Erreur lors du téléchargement du rapport Excel: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
}

