package vn.edu.iuh.fit.week05.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.week05.backend.models.SkillType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillResponseDTO {
    
    private Long id;
    private String skillName;
    private String skillDescription;
    private SkillType type;
}
