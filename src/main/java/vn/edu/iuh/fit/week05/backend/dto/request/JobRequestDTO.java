package vn.edu.iuh.fit.week05.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobRequestDTO {
    
    @NotBlank(message = "Job name is required")
    private String name;
    
    @NotBlank(message = "Job description is required")
    private String description;
    
    private Long companyId;
    
    @NotEmpty(message = "At least one skill is required")
    private List<JobSkillRequestDTO> jobSkills;
}
