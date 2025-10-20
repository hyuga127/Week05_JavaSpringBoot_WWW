package vn.edu.iuh.fit.week05.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.week05.backend.models.SkillType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillRequestDTO {
    
    @NotBlank(message = "Skill name is required")
    private String skillName;
    
    private String skillDescription;
    
    @NotNull(message = "Skill type is required")
    private SkillType type;
}
