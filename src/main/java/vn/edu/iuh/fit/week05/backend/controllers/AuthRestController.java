package vn.edu.iuh.fit.week05.backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.week05.backend.dto.request.CandidateSignupRequestDTO;
import vn.edu.iuh.fit.week05.backend.dto.request.CompanySignupRequestDTO;
import vn.edu.iuh.fit.week05.backend.dto.response.ApiResponse;
import vn.edu.iuh.fit.week05.backend.dto.response.AuthResponseDTO;
import vn.edu.iuh.fit.week05.backend.exception.DuplicateResourceException;
import vn.edu.iuh.fit.week05.backend.mapper.AddressMapper;
import vn.edu.iuh.fit.week05.backend.models.Address;
import vn.edu.iuh.fit.week05.backend.models.Candidate;
import vn.edu.iuh.fit.week05.backend.models.Company;
import vn.edu.iuh.fit.week05.backend.repositories.AddressRepository;
import vn.edu.iuh.fit.week05.backend.repositories.CandidateRepository;
import vn.edu.iuh.fit.week05.backend.repositories.CompanyRepository;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "APIs for user authentication and registration")
public class AuthRestController {
    
    @Autowired
    private CandidateRepository candidateRepository;
    
    @Autowired
    private CompanyRepository companyRepository;
    
    @Autowired
    private AddressRepository addressRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private AddressMapper addressMapper;
    
    @Operation(
        summary = "Register as Candidate",
        description = "Create a new candidate account. Email must be unique."
    )
    @PostMapping("/signup/candidate")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> registerCandidate(
            @Valid @RequestBody CandidateSignupRequestDTO signupDTO) {
        
        // Check if email already exists
        if (candidateRepository.findByEmail(signupDTO.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Candidate", "email", signupDTO.getEmail());
        }
        
        if (companyRepository.findByEmail(signupDTO.getEmail()).isPresent()) {
            throw new DuplicateResourceException("User", "email", signupDTO.getEmail());
        }
        
        // Create and save address
        Address address = addressMapper.toEntity(signupDTO.getAddress());
        Address savedAddress = addressRepository.save(address);
        
        // Create candidate
        Candidate candidate = new Candidate();
        candidate.setFullName(signupDTO.getFullName());
        candidate.setDob(signupDTO.getDob());
        candidate.setEmail(signupDTO.getEmail());
        candidate.setPassword(passwordEncoder.encode(signupDTO.getPassword())); // Encode password
        candidate.setPhone(signupDTO.getPhone());
        candidate.setAddress(savedAddress);
        
        // Save candidate
        Candidate savedCandidate = candidateRepository.save(candidate);
        
        // Create response
        AuthResponseDTO responseDTO = new AuthResponseDTO(
            savedCandidate.getId(),
            savedCandidate.getEmail(),
            "CANDIDATE",
            "Candidate account created successfully! Please login to continue."
        );
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful", responseDTO));
    }
    
    @Operation(
        summary = "Register as Company",
        description = "Create a new company account. Email must be unique."
    )
    @PostMapping("/signup/company")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> registerCompany(
            @Valid @RequestBody CompanySignupRequestDTO signupDTO) {
        
        // Check if email already exists
        if (companyRepository.findByEmail(signupDTO.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Company", "email", signupDTO.getEmail());
        }
        
        if (candidateRepository.findByEmail(signupDTO.getEmail()).isPresent()) {
            throw new DuplicateResourceException("User", "email", signupDTO.getEmail());
        }
        
        // Create and save address
        Address address = addressMapper.toEntity(signupDTO.getAddress());
        Address savedAddress = addressRepository.save(address);
        
        // Create company (ID will be auto-generated)
        Company company = new Company();
        company.setName(signupDTO.getName());
        company.setEmail(signupDTO.getEmail());
        company.setPassword(passwordEncoder.encode(signupDTO.getPassword())); // Encode password
        company.setPhone(signupDTO.getPhone());
        company.setAbout(signupDTO.getAbout());
        company.setWebUrl(signupDTO.getWebUrl());
        company.setAddress(savedAddress);
        
        // Save company
        Company savedCompany = companyRepository.save(company);
        
        // Create response
        AuthResponseDTO responseDTO = new AuthResponseDTO(
            savedCompany.getId(),
            savedCompany.getEmail(),
            "COMPANY",
            "Company account created successfully! Please login to continue."
        );
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful", responseDTO));
    }
    
    @Operation(
        summary = "Check email availability",
        description = "Check if an email address is available for registration"
    )
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Boolean>> checkEmailAvailability(
            @RequestParam String email) {
        
        boolean isAvailable = candidateRepository.findByEmail(email).isEmpty() 
                           && companyRepository.findByEmail(email).isEmpty();
        
        String message = isAvailable 
            ? "Email is available" 
            : "Email is already registered";
        
        return ResponseEntity.ok(ApiResponse.success(message, isAvailable));
    }
}
