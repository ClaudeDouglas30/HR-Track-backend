package com.hrtrack.controllers;


import com.hrtrack.domain.CongeStatus;
import com.hrtrack.domain.dtos.CongeDto;
import com.hrtrack.domain.entities.Conge;
import com.hrtrack.domain.responses.conge.CongeResponse;
import com.hrtrack.domain.responses.conge.ListCongeResponse;
import com.hrtrack.service.CongeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/conges")
public class CongeController {

    private final CongeService congeService;

    @GetMapping
    public ResponseEntity<ListCongeResponse> getAllListOfConge() {

        List<CongeDto> congeDtoList = congeService.getAllConge();

        return ResponseEntity.status(HttpStatus.OK).body(
                new ListCongeResponse(
                        congeDtoList != null,
                        congeDtoList
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<CongeResponse> getCongeById(@PathVariable UUID id) {
        CongeDto conge = congeService.getCongeById(id);

        return ResponseEntity.status(HttpStatus.OK).body(
                new CongeResponse(
                        conge != null,
                        conge
                )
        );
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<CongeResponse> acceptCongeById(@PathVariable UUID id) {
        CongeDto conge = congeService.changeCongeStatus(id, CongeStatus.ACCEPTER);

        return ResponseEntity.status(HttpStatus.OK).body(
                new CongeResponse(
                        conge != null,
                        conge
                )
        );
    }

    @PutMapping("/{id}/refuse")
    public ResponseEntity<CongeResponse> refusedCongeById(@PathVariable UUID id) {
        CongeDto conge = congeService.changeCongeStatus(id, CongeStatus.REFUSER);

        return ResponseEntity.status(HttpStatus.OK).body(
                new CongeResponse(
                        conge != null,
                        conge
                )
        );
    }

}
