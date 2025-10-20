package vn.edu.iuh.fit.week05.backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.week05.backend.dto.request.CompanyRequestDTO;
import vn.edu.iuh.fit.week05.backend.dto.response.ApiResponse;
import vn.edu.iuh.fit.week05.backend.dto.response.CompanyResponseDTO;
import vn.edu.iuh.fit.week05.backend.dto.response.PageResponse;
import vn.edu.iuh.fit.week05.backend.exception.ResourceNotFoundException;
import vn.edu.iuh.fit.week05.backend.mapper.CompanyMapper;
import vn.edu.iuh.fit.week05.backend.models.Company;
import vn.edu.iuh.fit.week05.backend.repositories.CompanyRepository;

@RestController
@RequestMapping("/api/v1/companies")
@Tag(name = "Company Management", description = "APIs for managing companies")
public class CompanyRestController {
    
    @Autowired
    private CompanyRepository companyRepository;
    
    @Autowired
    private CompanyMapper companyMapper;
    
    @Operation(summary = "Get all companies with pagination")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CompanyResponseDTO>>> getAllCompanies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Company> companyPage = companyRepository.findAll(pageable);
        PageResponse<CompanyResponseDTO> pageResponse = companyMapper.toPageResponse(companyPage);
        
        return ResponseEntity.ok(ApiResponse.success(pageResponse));
    }
    
    @Operation(summary = "Get company by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CompanyResponseDTO>> getCompanyById(@PathVariable Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", id));
        
        CompanyResponseDTO responseDTO = companyMapper.toDTO(company);
        return ResponseEntity.ok(ApiResponse.success(responseDTO));
    }
    
    @Operation(summary = "Create a new company")
    @PostMapping
    public ResponseEntity<ApiResponse<CompanyResponseDTO>> createCompany(
            @Valid @RequestBody CompanyRequestDTO requestDTO) {
        
        Company company = companyMapper.toEntity(requestDTO);
        Company savedCompany = companyRepository.save(company);
        
        CompanyResponseDTO responseDTO = companyMapper.toDTO(savedCompany);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Company created successfully", responseDTO));
    }
    
    @Operation(summary = "Update company information")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CompanyResponseDTO>> updateCompany(
            @PathVariable Long id,
            @Valid @RequestBody CompanyRequestDTO requestDTO) {
        
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", id));
        
        companyMapper.updateEntityFromDTO(requestDTO, company);
        Company updatedCompany = companyRepository.save(company);
        
        CompanyResponseDTO responseDTO = companyMapper.toDTO(updatedCompany);
        return ResponseEntity.ok(ApiResponse.success("Company updated successfully", responseDTO));
    }
    
    @Operation(summary = "Delete a company")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteCompany(@PathVariable Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", id));
        
        companyRepository.delete(company);
        
        return ResponseEntity.ok(ApiResponse.success("Company deleted successfully", null));
    }
}
