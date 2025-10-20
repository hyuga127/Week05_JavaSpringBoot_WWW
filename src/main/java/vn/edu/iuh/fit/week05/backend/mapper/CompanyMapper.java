package vn.edu.iuh.fit.week05.backend.mapper;

import org.mapstruct.*;
import org.springframework.data.domain.Page;
import vn.edu.iuh.fit.week05.backend.dto.request.CompanyRequestDTO;
import vn.edu.iuh.fit.week05.backend.dto.response.CompanyResponseDTO;
import vn.edu.iuh.fit.week05.backend.dto.response.PageResponse;
import vn.edu.iuh.fit.week05.backend.models.Company;

import java.util.List;

@Mapper(componentModel = "spring", uses = {AddressMapper.class})
public interface CompanyMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jobs", ignore = true)
    Company toEntity(CompanyRequestDTO dto);
    
    CompanyResponseDTO toDTO(Company entity);
    
    List<CompanyResponseDTO> toDTOList(List<Company> entities);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jobs", ignore = true)
    void updateEntityFromDTO(CompanyRequestDTO dto, @MappingTarget Company entity);
    
    default PageResponse<CompanyResponseDTO> toPageResponse(Page<Company> page) {
        PageResponse<CompanyResponseDTO> response = new PageResponse<>();
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
