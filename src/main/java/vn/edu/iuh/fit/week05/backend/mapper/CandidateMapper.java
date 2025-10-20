package vn.edu.iuh.fit.week05.backend.mapper;

import org.mapstruct.*;
import org.springframework.data.domain.Page;
import vn.edu.iuh.fit.week05.backend.dto.request.CandidateRequestDTO;
import vn.edu.iuh.fit.week05.backend.dto.response.CandidateResponseDTO;
import vn.edu.iuh.fit.week05.backend.dto.response.PageResponse;
import vn.edu.iuh.fit.week05.backend.models.Candidate;

import java.util.List;

@Mapper(componentModel = "spring", uses = {AddressMapper.class, CandidateSkillMapper.class})
public interface CandidateMapper {
    
    @Mapping(target = "id", ignore = true)
//    @Mapping(target = "role", constant = "ROLE_CANDIDATE")
    @Mapping(target = "candidateSkills", ignore = true)
    @Mapping(target = "experiences", ignore = true)
    Candidate toEntity(CandidateRequestDTO dto);
    
    CandidateResponseDTO toDTO(Candidate entity);
    
    List<CandidateResponseDTO> toDTOList(List<Candidate> entities);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "candidateSkills", ignore = true)
    @Mapping(target = "experiences", ignore = true)
    void updateEntityFromDTO(CandidateRequestDTO dto, @MappingTarget Candidate entity);
    
    default PageResponse<CandidateResponseDTO> toPageResponse(Page<Candidate> page) {
        PageResponse<CandidateResponseDTO> response = new PageResponse<>();
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
