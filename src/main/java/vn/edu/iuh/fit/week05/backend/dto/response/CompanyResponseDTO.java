package vn.edu.iuh.fit.week05.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyResponseDTO {
    
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String about;
    private String webUrl;
    private AddressResponseDTO address;
    // Note: password is intentionally excluded for security
}
