package vn.edu.iuh.fit.week05.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidateResponseDTO {
    
    private Long id;
    private String fullName;
    private LocalDate dob;
    private String email;
    private String phone;
    private String avatarUrl;
    private AddressResponseDTO address;
    private List<CandidateSkillResponseDTO> candidateSkills;
    // Note: password is intentionally excluded for security
}
