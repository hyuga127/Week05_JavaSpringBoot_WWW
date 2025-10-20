package vn.edu.iuh.fit.week05.backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.week05.backend.dto.request.CandidateRequestDTO;
import vn.edu.iuh.fit.week05.backend.dto.request.CandidateSkillRequestDTO;
import vn.edu.iuh.fit.week05.backend.dto.response.ApiResponse;
import vn.edu.iuh.fit.week05.backend.dto.response.CandidateResponseDTO;
import vn.edu.iuh.fit.week05.backend.dto.response.PageResponse;
import vn.edu.iuh.fit.week05.backend.dto.response.SkillResponseDTO;
import vn.edu.iuh.fit.week05.backend.exception.ResourceNotFoundException;
import vn.edu.iuh.fit.week05.backend.mapper.CandidateMapper;
import vn.edu.iuh.fit.week05.backend.mapper.SkillMapper;
import vn.edu.iuh.fit.week05.backend.models.Candidate;
import vn.edu.iuh.fit.week05.backend.models.CandidateSkill;
import vn.edu.iuh.fit.week05.backend.models.CandidateSkillId;
import vn.edu.iuh.fit.week05.backend.models.Skill;
import vn.edu.iuh.fit.week05.backend.services.CandidateService;
import vn.edu.iuh.fit.week05.backend.services.CandidateSkillService;
import vn.edu.iuh.fit.week05.backend.services.SkillService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/candidates")
@Tag(name = "Candidate Management", description = "APIs for managing candidates")
public class CandidateRestController {
    
    @Autowired
    private CandidateService candidateService;
    
    @Autowired
    private CandidateSkillService candidateSkillService;
    
    @Autowired
    private SkillService skillService;
    
    @Autowired
    private CandidateMapper candidateMapper;
    
    @Autowired
    private SkillMapper skillMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private vn.edu.iuh.fit.week05.backend.services.FileStorageService fileStorageService;
    
    @Operation(summary = "Get all candidates with pagination")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CandidateResponseDTO>>> getAllCandidates(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        
        Page<Candidate> candidatePage = candidateService.findAll(page, size, sortBy, direction);
        PageResponse<CandidateResponseDTO> pageResponse = candidateMapper.toPageResponse(candidatePage);
        
        return ResponseEntity.ok(ApiResponse.success(pageResponse));
    }
    
    @Operation(summary = "Get candidate by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CandidateResponseDTO>> getCandidateById(@PathVariable Long id) {
        Candidate candidate = candidateService.findById(id);
        if (candidate == null) {
            throw new ResourceNotFoundException("Candidate", "id", id);
        }
        
        CandidateResponseDTO responseDTO = candidateMapper.toDTO(candidate);
        return ResponseEntity.ok(ApiResponse.success(responseDTO));
    }
    
    @Operation(summary = "Create a new candidate")
    @PostMapping
    public ResponseEntity<ApiResponse<CandidateResponseDTO>> createCandidate(
            @Valid @RequestBody CandidateRequestDTO requestDTO) {
        
        Candidate candidate = candidateMapper.toEntity(requestDTO);
        // Here you would save the candidate using a service method
        // For now, this is a placeholder
        
        CandidateResponseDTO responseDTO = candidateMapper.toDTO(candidate);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Candidate created successfully", responseDTO));
    }

    @Operation(summary = "Update candidate information")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CandidateResponseDTO>> updateCandidate(
            @PathVariable Long id,
            @Valid @RequestBody CandidateRequestDTO requestDTO) {

        Candidate candidate = candidateService.findById(id);
        if (candidate == null) {
            throw new ResourceNotFoundException("Candidate", "id", id);
        }

        // Save the current password
        String currentPassword = candidate.getPassword();

        // Update all fields except password
        candidateMapper.updateEntityFromDTO(requestDTO, candidate);

        // Handle password separately
        if (requestDTO.getPassword() != null && !requestDTO.getPassword().isEmpty()) {
            candidate.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
        } else {
            candidate.setPassword(currentPassword);
        }

        Candidate updatedCandidate = candidateService.save(candidate);
        return ResponseEntity.ok(ApiResponse.success(
                "Candidate updated successfully",
                candidateMapper.toDTO(updatedCandidate)
        ));
    }
    
    @Operation(summary = "Upload candidate avatar")
    @PostMapping("/{id}/avatar")
    public ResponseEntity<ApiResponse<String>> uploadAvatar(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        
        Candidate candidate = candidateService.findById(id);
        if (candidate == null) {
            throw new ResourceNotFoundException("Candidate", "id", id);
        }
        
        // Delete old avatar if exists
        if (candidate.getAvatarUrl() != null) {
            fileStorageService.deleteFile(candidate.getAvatarUrl());
        }
        
        // Store new avatar
        String avatarUrl = fileStorageService.storeFile(file);
        candidateService.updateAvatar(id, avatarUrl);
        
        return ResponseEntity.ok(ApiResponse.success("Avatar uploaded successfully", avatarUrl));
    }
    
    @Operation(summary = "Update candidate skills")
    @PutMapping("/{id}/skills")
    public ResponseEntity<ApiResponse<String>> updateCandidateSkills(
            @PathVariable Long id,
            @Valid @RequestBody List<CandidateSkillRequestDTO> skillsDTO) {
        
        Candidate candidate = candidateService.findById(id);
        if (candidate == null) {
            throw new ResourceNotFoundException("Candidate", "id", id);
        }
        
        // Get existing skills
        List<CandidateSkill> existingSkills = candidateSkillService.findCandidateSkillByCandidateId(id);
        
        // Update or create skills from DTO
        for (CandidateSkillRequestDTO skillDTO : skillsDTO) {
            CandidateSkillId candidateSkillId = new CandidateSkillId(skillDTO.getSkillId(), id);
            
            CandidateSkill candidateSkill = candidateSkillService.findById(candidateSkillId)
                    .orElse(new CandidateSkill(candidateSkillId));
            
            candidateSkill.setCandidate(candidate);
            candidateSkill.setSkill(skillService.getSkillById(skillDTO.getSkillId()));
            candidateSkill.setSkillLevel(skillDTO.getSkillLevel());
            candidateSkill.setMoreInfos(skillDTO.getMoreInfos());
            
            candidateSkillService.save(candidateSkill);
        }
        
        // Remove skills not in the new list
        List<Long> newSkillIds = skillsDTO.stream()
                .map(CandidateSkillRequestDTO::getSkillId)
                .toList();
        
        for (CandidateSkill existingSkill : existingSkills) {
            if (!newSkillIds.contains(existingSkill.getId().getSkillId())) {
                candidateSkillService.delete(existingSkill);
            }
        }
        
        return ResponseEntity.ok(ApiResponse.success("Skills updated successfully", null));
    }
    
    @Operation(summary = "Get skill suggestions for a candidate")
    @GetMapping("/{id}/skill-suggestions")
    public ResponseEntity<ApiResponse<List<SkillResponseDTO>>> getSkillSuggestions(
            @PathVariable Long id) {
        
        Candidate candidate = candidateService.findById(id);
        if (candidate == null) {
            throw new ResourceNotFoundException("Candidate", "id", id);
        }
        
        List<Skill> suggestedSkills = skillService.getSuggestedSkills(id);
        List<SkillResponseDTO> skillDTOs = skillMapper.toDTOList(suggestedSkills);
        
        return ResponseEntity.ok(ApiResponse.success(skillDTOs));
    }
}
