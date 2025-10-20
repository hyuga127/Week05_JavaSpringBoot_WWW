package vn.edu.iuh.fit.week05.backend.mapper;

import org.mapstruct.*;
import vn.edu.iuh.fit.week05.backend.dto.request.SkillRequestDTO;
import vn.edu.iuh.fit.week05.backend.dto.response.SkillResponseDTO;
import vn.edu.iuh.fit.week05.backend.models.Skill;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SkillMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jobSkills", ignore = true)
    Skill toEntity(SkillRequestDTO dto);
    
    SkillResponseDTO toDTO(Skill entity);
    
    List<SkillResponseDTO> toDTOList(List<Skill> entities);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jobSkills", ignore = true)
    void updateEntityFromDTO(SkillRequestDTO dto, @MappingTarget Skill entity);
}
