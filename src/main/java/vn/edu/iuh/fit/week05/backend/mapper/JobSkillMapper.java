package vn.edu.iuh.fit.week05.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.iuh.fit.week05.backend.dto.response.JobSkillResponseDTO;
import vn.edu.iuh.fit.week05.backend.models.JobSkill;

@Mapper(componentModel = "spring")
public interface JobSkillMapper {
    
    @Mapping(source = "skill.id", target = "skillId")
    @Mapping(source = "skill.skillName", target = "skillName")
    JobSkillResponseDTO toDTO(JobSkill entity);
}
