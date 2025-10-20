package vn.edu.iuh.fit.week05.backend.mapper;

import org.mapstruct.*;
import org.springframework.data.domain.Page;
import vn.edu.iuh.fit.week05.backend.dto.request.JobRequestDTO;
import vn.edu.iuh.fit.week05.backend.dto.response.JobResponseDTO;
import vn.edu.iuh.fit.week05.backend.dto.response.PageResponse;
import vn.edu.iuh.fit.week05.backend.models.Job;

import java.util.List;

@Mapper(componentModel = "spring", uses = {CompanyMapper.class, JobSkillMapper.class})
public interface JobMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "jobSkills", ignore = true)
    Job toEntity(JobRequestDTO dto);
    
    JobResponseDTO toDTO(Job entity);
    
    List<JobResponseDTO> toDTOList(List<Job> entities);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "jobSkills", ignore = true)
    void updateEntityFromDTO(JobRequestDTO dto, @MappingTarget Job entity);
    
    default PageResponse<JobResponseDTO> toPageResponse(Page<Job> page) {
        PageResponse<JobResponseDTO> response = new PageResponse<>();
        response.setContent(toDTOList(page.getContent()));
        response.setPageNumber(page.getNumber());
        response.setPageSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setLast(page.isLast());
        response.setFirst(page.isFirst());
        return response;
    }
}
