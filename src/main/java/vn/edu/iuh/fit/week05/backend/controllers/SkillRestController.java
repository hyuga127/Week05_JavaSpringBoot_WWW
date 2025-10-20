package vn.edu.iuh.fit.week05.backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.week05.backend.dto.request.SkillRequestDTO;
import vn.edu.iuh.fit.week05.backend.dto.response.ApiResponse;
import vn.edu.iuh.fit.week05.backend.dto.response.SkillResponseDTO;
import vn.edu.iuh.fit.week05.backend.exception.ResourceNotFoundException;
import vn.edu.iuh.fit.week05.backend.mapper.SkillMapper;
import vn.edu.iuh.fit.week05.backend.models.Skill;
import vn.edu.iuh.fit.week05.backend.services.SkillService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/skills")
@Tag(name = "Skill Management", description = "APIs for managing skills")
public class SkillRestController {
    
    @Autowired
    private SkillService skillService;
    
    @Autowired
    private SkillMapper skillMapper;
    
    @Operation(summary = "Get all skills")
    @GetMapping
    public ResponseEntity<ApiResponse<List<SkillResponseDTO>>> getAllSkills() {
        List<Skill> skills = skillService.getAllSkills();
        List<SkillResponseDTO> skillDTOs = skillMapper.toDTOList(skills);
        
        return ResponseEntity.ok(ApiResponse.success(skillDTOs));
    }
    
    @Operation(summary = "Get skill by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SkillResponseDTO>> getSkillById(@PathVariable Long id) {
        Skill skill = skillService.getSkillById(id);
        if (skill == null) {
            throw new ResourceNotFoundException("Skill", "id", id);
        }
        
        SkillResponseDTO responseDTO = skillMapper.toDTO(skill);
        return ResponseEntity.ok(ApiResponse.success(responseDTO));
    }
    
    @Operation(summary = "Create a new skill")
    @PostMapping
    public ResponseEntity<ApiResponse<SkillResponseDTO>> createSkill(
            @Valid @RequestBody SkillRequestDTO requestDTO) {
        
        Skill skill = skillMapper.toEntity(requestDTO);
        Skill savedSkill = skillService.saveSkill(skill);
        
        SkillResponseDTO responseDTO = skillMapper.toDTO(savedSkill);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Skill created successfully", responseDTO));
    }
    
    @Operation(summary = "Update skill information")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SkillResponseDTO>> updateSkill(
            @PathVariable Long id,
            @Valid @RequestBody SkillRequestDTO requestDTO) {
        
        Skill skill = skillService.getSkillById(id);
        if (skill == null) {
            throw new ResourceNotFoundException("Skill", "id", id);
        }
        
        skillMapper.updateEntityFromDTO(requestDTO, skill);
        Skill updatedSkill = skillService.saveSkill(skill);
        
        SkillResponseDTO responseDTO = skillMapper.toDTO(updatedSkill);
        return ResponseEntity.ok(ApiResponse.success("Skill updated successfully", responseDTO));
    }
    
    @Operation(summary = "Delete a skill")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteSkill(@PathVariable Long id) {
        Skill skill = skillService.getSkillById(id);
        if (skill == null) {
            throw new ResourceNotFoundException("Skill", "id", id);
        }
        
        skillService.deleteSkill(id);
        
        return ResponseEntity.ok(ApiResponse.success("Skill deleted successfully", null));
    }
}
