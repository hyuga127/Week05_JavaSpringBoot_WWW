package vn.edu.iuh.fit.week05.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.week05.backend.models.SkillLevel;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobSkillRequestDTO {
    
    @NotNull(message = "Skill ID is required")
    private Long skillId;
    
    @NotNull(message = "Skill level is required")
    private SkillLevel skillLevel;
    
    private String moreInfos;
}
