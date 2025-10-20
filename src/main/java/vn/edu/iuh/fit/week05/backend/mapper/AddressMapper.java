package vn.edu.iuh.fit.week05.backend.mapper;

import org.mapstruct.*;
import vn.edu.iuh.fit.week05.backend.dto.request.AddressRequestDTO;
import vn.edu.iuh.fit.week05.backend.dto.response.AddressResponseDTO;
import vn.edu.iuh.fit.week05.backend.models.Address;

@Mapper(componentModel = "spring")
public interface AddressMapper {
    
    @Mapping(target = "id", ignore = true)
    Address toEntity(AddressRequestDTO dto);
    
    AddressResponseDTO toDTO(Address entity);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateEntityFromDTO(AddressRequestDTO dto, @MappingTarget Address entity);
}
