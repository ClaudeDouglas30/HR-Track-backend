package com.hrtrack.service.implementation;

import com.hrtrack.domain.PointageStatus;
import com.hrtrack.domain.Role;
import com.hrtrack.domain.entities.Pointage;
import com.hrtrack.domain.entities.User;
import com.hrtrack.repository.PointageRepository;
import com.hrtrack.repository.UserRepository;
import com.hrtrack.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Service d'implémentation pour la génération de rapports.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final UserRepository userRepository;
    private final PointageRepository pointageRepository;

    /**
     * Génère un rapport Excel des absences et retards pour un mois donné.
     * 
     * @param yearMonth Le mois pour lequel générer le rapport
     * @return ByteArrayOutputStream contenant le fichier Excel
     */
    @Override
    @Transactional(readOnly = true)
    public ByteArrayOutputStream generateAttendanceReport(YearMonth yearMonth) {
        // Récupérer tous les employés
        List<User> employees = userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.EMPLOYEE)
                .toList();

        // Calculer les dates de début et fin du mois
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        
        Date startDateObj = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDateObj = Date.from(endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());

        // Créer le workbook Excel
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Rapport Absences et Retards");

            // Créer les styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            // Créer l'en-tête
            int rowNum = 0;
            Row headerRow = sheet.createRow(rowNum++);
            
            String[] headers = {"Nom", "Prénom", "Email", "Nombre d'absences", "Nombre de retards"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Remplir les données
            for (User employee : employees) {
                // Récupérer les pointages de l'employé pour le mois
                List<Pointage> pointages = pointageRepository.findByUserAndDateBetween(
                        employee, startDateObj, endDateObj);

                // Calculer les absences (pointages sans heure d'arrivée ni de départ)
                long absences = pointages.stream()
                        .filter(p -> p.getHeureArrivee() == null && p.getHeureDepart() == null)
                        .count();

                // Calculer les retards
                long retards = pointages.stream()
                        .filter(p -> p.getStatut() == PointageStatus.RETARD)
                        .count();

                Row row = sheet.createRow(rowNum++);
                
                Cell cell0 = row.createCell(0);
                cell0.setCellValue(employee.getNom() != null ? employee.getNom() : "");
                cell0.setCellStyle(dataStyle);

                Cell cell1 = row.createCell(1);
                cell1.setCellValue(employee.getPrenom() != null ? employee.getPrenom() : "");
                cell1.setCellStyle(dataStyle);

                Cell cell2 = row.createCell(2);
                cell2.setCellValue(employee.getEmail() != null ? employee.getEmail() : "");
                cell2.setCellStyle(dataStyle);

                Cell cell3 = row.createCell(3);
                cell3.setCellValue(absences);
                cell3.setCellStyle(dataStyle);

                Cell cell4 = row.createCell(4);
                cell4.setCellValue(retards);
                cell4.setCellStyle(dataStyle);
            }

            // Ajuster la largeur des colonnes
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                // Ajouter un peu de marge
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
            }

            // Écrire dans un ByteArrayOutputStream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            
            log.info("Rapport Excel généré pour le mois: {}", yearMonth);
            return outputStream;

        } catch (IOException e) {
            log.error("Erreur lors de la génération du rapport Excel: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la génération du rapport Excel", e);
        }
    }

    /**
     * Crée un style pour les en-têtes.
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    /**
     * Crée un style pour les données.
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }
}

