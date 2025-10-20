package vn.edu.iuh.fit.week05.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {
    
    private Long userId;
    private String email;
    private String role;
    private String message;
    
    public AuthResponseDTO(String message) {
        this.message = message;
    }
}
