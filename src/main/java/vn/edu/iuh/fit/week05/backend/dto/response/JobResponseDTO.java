package vn.edu.iuh.fit.week05.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobResponseDTO {
    
    private Long id;
    private String name;
    private String description;
    private CompanyResponseDTO company;
    private List<JobSkillResponseDTO> jobSkills;
}
