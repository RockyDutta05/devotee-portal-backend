package com.devoteeportal.backend.controller;

import com.devoteeportal.backend.dto.CompanyDto;
import com.devoteeportal.backend.dto.CompanyRequest;
import com.devoteeportal.backend.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    public ResponseEntity<List<CompanyDto>> searchCompanies(@RequestParam(required = false) String search) {
        return ResponseEntity.ok(companyService.searchCompanies(search));
    }

    @PostMapping
    public ResponseEntity<CompanyDto> createCompany(Authentication authentication, @RequestBody CompanyRequest request) {
        return ResponseEntity.ok(companyService.createCompany(authentication.getName(), request));
    }
}
