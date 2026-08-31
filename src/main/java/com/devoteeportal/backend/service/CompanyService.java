package com.devoteeportal.backend.service;

import com.devoteeportal.backend.dto.CompanyDto;
import com.devoteeportal.backend.dto.CompanyRequest;
import com.devoteeportal.backend.entity.Company;
import com.devoteeportal.backend.entity.User;
import com.devoteeportal.backend.repository.CompanyRepository;
import com.devoteeportal.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public List<CompanyDto> searchCompanies(String query) {
        if (query == null || query.isBlank()) {
            return companyRepository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
        }
        return companyRepository.findByNameContainingIgnoreCase(query)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CompanyDto createCompany(String email, CompanyRequest request) {
        String trimmedName = request.getName().trim();
        if (companyRepository.existsByNameIgnoreCase(trimmedName)) {
            // Instead of throwing an error, we should probably just return the existing company?
            // The frontend autoComplete creates it if it doesn't exist.
            // Returning the existing one avoids dupes and also satisfies the autocomplete requirement.
            Company existing = companyRepository.findByNameContainingIgnoreCase(trimmedName).stream()
                    .filter(c -> c.getName().equalsIgnoreCase(trimmedName))
                    .findFirst()
                    .orElse(null);
            if (existing != null) {
                return mapToDto(existing);
            }
        }
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Company company = Company.builder()
                .name(trimmedName)
                .addedByUserId(user)
                .approved(false) // requires admin approval eventually if needed
                .build();

        return mapToDto(companyRepository.save(company));
    }

    private CompanyDto mapToDto(Company company) {
        return CompanyDto.builder()
                .id(company.getId())
                .name(company.getName())
                .approved(company.getApproved())
                .build();
    }
}
