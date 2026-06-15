package com.inditex.supplier.infrastructure.rest.controller;

import com.inditex.supplier.application.service.CandidateService;
import com.inditex.supplier.domain.model.SustainabilityRating;
import com.inditex.supplier.infrastructure.rest.dto.AcceptCandidateRequest;
import com.inditex.supplier.infrastructure.rest.dto.CandidateResponse;
import com.inditex.supplier.infrastructure.rest.dto.CreateCandidateRequest;
import com.inditex.supplier.infrastructure.rest.mapper.CandidateApiMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/candidates")
@Validated
public class CandidateController {

    private final CandidateService candidateService;
    private final CandidateApiMapper mapper;

    public CandidateController(CandidateService candidateService, CandidateApiMapper mapper) {
        this.candidateService = candidateService;
        this.mapper = mapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CandidateResponse createCandidate(@Valid @RequestBody CreateCandidateRequest request) {
        return mapper.toResponse(
                candidateService.createCandidate(request.duns(), request.name(), request.country(), request.annualTurnover()));
    }

    @GetMapping("/{duns}")
    public CandidateResponse getCandidate(
            @PathVariable @Min(100000000) @Max(999999999) int duns) {
        return mapper.toResponse(candidateService.getCandidate(duns));
    }

    @PostMapping("/{duns}/accept")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void acceptCandidate(
            @PathVariable @Min(100000000) @Max(999999999) int duns,
            @Valid @RequestBody AcceptCandidateRequest request) {
        candidateService.acceptCandidate(duns, SustainabilityRating.valueOf(request.sustainabilityRating()));
    }

    @PostMapping("/{duns}/refuse")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void refuseCandidate(
            @PathVariable @Min(100000000) @Max(999999999) int duns) {
        candidateService.refuseCandidate(duns);
    }
}
