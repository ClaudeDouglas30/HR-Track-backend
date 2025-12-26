package com.hrtrack.controllers;

import com.hrtrack.domain.dtos.StatsUserDto;
import com.hrtrack.domain.dtos.StatsRhDto;
import com.hrtrack.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
public class StatsMobileController {
    private final StatsService statsService;

    @PreAuthorize("hasAuthority('EMPLOYEE')")
    @GetMapping("/user")
    public ResponseEntity<StatsUserDto> getUserStats(Principal principal) {
        return ResponseEntity.ok(statsService.getUserStats(principal.getName()));
    }

    @PreAuthorize("hasAnyAuthority('RH','ADMIN')")
    @GetMapping("/rh")
    public ResponseEntity<StatsRhDto> getRhStats() {
        return ResponseEntity.ok(statsService.getRhStats());
    }
}