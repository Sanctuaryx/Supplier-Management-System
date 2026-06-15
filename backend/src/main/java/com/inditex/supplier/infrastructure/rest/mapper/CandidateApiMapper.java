package com.inditex.supplier.infrastructure.rest.mapper;

import com.inditex.supplier.domain.model.Candidate;
import com.inditex.supplier.infrastructure.rest.dto.CandidateResponse;
import org.springframework.stereotype.Component;

@Component
public class CandidateApiMapper {

    public CandidateResponse toResponse(Candidate candidate) {
        return new CandidateResponse(
                candidate.getDuns().value(),
                candidate.getName(),
                candidate.getCountry().code(),
                candidate.getAnnualTurnover()
        );
    }
}
