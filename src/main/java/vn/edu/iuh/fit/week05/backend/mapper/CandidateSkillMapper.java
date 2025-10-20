package vn.edu.iuh.fit.week05.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.iuh.fit.week05.backend.dto.response.CandidateSkillResponseDTO;
import vn.edu.iuh.fit.week05.backend.models.CandidateSkill;

@Mapper(componentModel = "spring")
public interface CandidateSkillMapper {
    
    @Mapping(source = "skill.id", target = "skillId")
    @Mapping(source = "skill.skillName", target = "skillName")
    CandidateSkillResponseDTO toDTO(CandidateSkill entity);
}
