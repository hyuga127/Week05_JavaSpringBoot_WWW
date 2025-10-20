package vn.edu.iuh.fit.week05.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.week05.backend.models.SkillLevel;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidateSkillResponseDTO {
    
    private Long skillId;
    private String skillName;
    private SkillLevel skillLevel;
    private String moreInfos;
}
