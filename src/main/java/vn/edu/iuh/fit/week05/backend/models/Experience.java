package vn.edu.iuh.fit.week05.backend.models;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "experience", schema = "works")
public class Experience {
    @Id
    @Column(name = "exp_id", nullable = false)
    private Long id;

    @Column(name = "from_date", nullable = false)
    private LocalDate fromDate;

    @Column(name = "to_date", nullable = false)
    private LocalDate toDate;

    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "company", nullable = false)
    private String companyName;

    @Column(name = "work_desc", length = 2000)
    private String workDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "can_id")
    private Candidate candidate;


}
