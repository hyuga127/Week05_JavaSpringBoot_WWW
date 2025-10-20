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
import vn.edu.iuh.fit.week05.backend.dto.request.JobRequestDTO;
import vn.edu.iuh.fit.week05.backend.dto.request.JobSkillRequestDTO;
import vn.edu.iuh.fit.week05.backend.dto.response.ApiResponse;
import vn.edu.iuh.fit.week05.backend.dto.response.JobResponseDTO;
import vn.edu.iuh.fit.week05.backend.dto.response.PageResponse;
import vn.edu.iuh.fit.week05.backend.exception.ResourceNotFoundException;
import vn.edu.iuh.fit.week05.backend.mapper.JobMapper;
import vn.edu.iuh.fit.week05.backend.models.*;
import vn.edu.iuh.fit.week05.backend.repositories.CompanyRepository;
import vn.edu.iuh.fit.week05.backend.repositories.JobRepository;
import vn.edu.iuh.fit.week05.backend.services.JobService;
import vn.edu.iuh.fit.week05.backend.services.JobSkillService;
import vn.edu.iuh.fit.week05.backend.services.SkillService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/jobs")
@Tag(name = "Job Management", description = "APIs for managing jobs")
public class JobRestController {
    
    @Autowired
    private JobService jobService;
    
    @Autowired
    private JobRepository jobRepository;
    
    @Autowired
    private CompanyRepository companyRepository;
    
    @Autowired
    private JobSkillService jobSkillService;
    
    @Autowired
    private SkillService skillService;
    
    @Autowired
    private JobMapper jobMapper;
    
    @Operation(summary = "Get all jobs with pagination")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<JobResponseDTO>>> getAllJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Job> jobPage = jobRepository.findAll(pageable);
        PageResponse<JobResponseDTO> pageResponse = jobMapper.toPageResponse(jobPage);
        
        return ResponseEntity.ok(ApiResponse.success(pageResponse));
    }
    
    @Operation(summary = "Get job by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JobResponseDTO>> getJobById(@PathVariable Long id) {
        Job job = jobService.getJobById(id);
        if (job == null) {
            throw new ResourceNotFoundException("Job", "id", id);
        }
        
        JobResponseDTO responseDTO = jobMapper.toDTO(job);
        return ResponseEntity.ok(ApiResponse.success(responseDTO));
    }
    
    @Operation(summary = "Get matching jobs for a candidate")
    @GetMapping("/matching")
    public ResponseEntity<ApiResponse<PageResponse<JobResponseDTO>>> getMatchingJobs(
            @RequestParam Long candidateId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Job> matchingJobs = jobService.findMatchingJobsByCandidateId(candidateId, pageable);
        PageResponse<JobResponseDTO> pageResponse = jobMapper.toPageResponse(matchingJobs);
        
        return ResponseEntity.ok(ApiResponse.success(pageResponse));
    }
    
    @Operation(summary = "Get jobs by company ID")
    @GetMapping("/company/{companyId}")
    public ResponseEntity<ApiResponse<PageResponse<JobResponseDTO>>> getJobsByCompany(
            @PathVariable Long companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Job> jobPage = jobRepository.findByCompanyId(companyId, pageable);
        PageResponse<JobResponseDTO> pageResponse = jobMapper.toPageResponse(jobPage);
        
        return ResponseEntity.ok(ApiResponse.success(pageResponse));
    }
    
    @Operation(summary = "Create a new job")
    @PostMapping
    public ResponseEntity<ApiResponse<JobResponseDTO>> createJob(
            @Valid @RequestBody JobRequestDTO requestDTO) {
        
        // Get company
        Company company = companyRepository.findById(requestDTO.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", requestDTO.getCompanyId()));
        
        // Create job
        Job job = jobMapper.toEntity(requestDTO);
        job.setCompany(company);
        Job savedJob = jobService.saveJob(job);
        
        // Add job skills
        for (JobSkillRequestDTO skillDTO : requestDTO.getJobSkills()) {
            Skill skill = skillService.getSkillById(skillDTO.getSkillId());
            if (skill == null) {
                throw new ResourceNotFoundException("Skill", "id", skillDTO.getSkillId());
            }
            
            JobSkill jobSkill = new JobSkill();
            JobSkillId jobSkillId = new JobSkillId(savedJob.getId(), skillDTO.getSkillId());
            jobSkill.setId(jobSkillId);
            jobSkill.setJob(savedJob);
            jobSkill.setSkill(skill);
            jobSkill.setSkillLevel(skillDTO.getSkillLevel());
            jobSkill.setMoreInfos(skillDTO.getMoreInfos());
            
            jobSkillService.save(jobSkill);
        }
        
        // Reload job with skills
        Job reloadedJob = jobService.getJobById(savedJob.getId());
        JobResponseDTO responseDTO = jobMapper.toDTO(reloadedJob);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Job created successfully", responseDTO));
    }
    
    @Operation(summary = "Update job information")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<JobResponseDTO>> updateJob(
            @PathVariable Long id,
            @Valid @RequestBody JobRequestDTO requestDTO) {
        
        Job job = jobService.getJobById(id);
        if (job == null) {
            throw new ResourceNotFoundException("Job", "id", id);
        }
        
        // Update basic job info
        jobMapper.updateEntityFromDTO(requestDTO, job);
        jobService.saveJob(job);
        
        // Update job skills
        List<JobSkill> existingJobSkills = jobSkillService.findJobSkillByJobId(id);
        
        // Add or update skills
        for (JobSkillRequestDTO skillDTO : requestDTO.getJobSkills()) {
            JobSkillId jobSkillId = new JobSkillId(id, skillDTO.getSkillId());
            
            JobSkill jobSkill = jobSkillService.findById(jobSkillId)
                    .orElse(new JobSkill(jobSkillId));
            
            jobSkill.setJob(job);
            jobSkill.setSkill(skillService.getSkillById(skillDTO.getSkillId()));
            jobSkill.setSkillLevel(skillDTO.getSkillLevel());
            jobSkill.setMoreInfos(skillDTO.getMoreInfos());
            
            jobSkillService.save(jobSkill);
        }
        
        // Remove skills not in the new list
        List<Long> newSkillIds = requestDTO.getJobSkills().stream()
                .map(JobSkillRequestDTO::getSkillId)
                .toList();
        
        for (JobSkill existingSkill : existingJobSkills) {
            if (!newSkillIds.contains(existingSkill.getId().getSkillId())) {
                jobSkillService.delete(existingSkill);
            }
        }
        
        // Reload job with updated skills
        Job updatedJob = jobService.getJobById(id);
        JobResponseDTO responseDTO = jobMapper.toDTO(updatedJob);
        
        return ResponseEntity.ok(ApiResponse.success("Job updated successfully", responseDTO));
    }
    
    @Operation(summary = "Delete a job")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteJob(@PathVariable Long id) {
        Job job = jobService.getJobById(id);
        if (job == null) {
            throw new ResourceNotFoundException("Job", "id", id);
        }
        
        jobService.deleteJob(id);
        
        return ResponseEntity.ok(ApiResponse.success("Job deleted successfully", null));
    }
}
