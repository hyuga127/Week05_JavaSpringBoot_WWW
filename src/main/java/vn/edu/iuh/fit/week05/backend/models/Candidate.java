package vn.edu.iuh.fit.week05.backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "candidate", schema = "works")
public class Candidate implements UserDetails {
    @Id
    @Column(name = "can_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dob", nullable = false)
    private LocalDate dob;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "phone", nullable = false, length = 15)
    private String phone;

    @Column(name = "role", nullable = false)
    private String role = "ROLE_CANDIDATE";

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "address", nullable = false)
    private Address address;

    @OneToMany(mappedBy = "candidate", fetch = FetchType.LAZY)
    private List<CandidateSkill> candidateSkills;

    @OneToMany(mappedBy = "candidate", fetch = FetchType.LAZY)
    private List<Experience> experiences;

    public Candidate(String s, LocalDate of, Address add, String s1, String s2, String pass) {
        this.fullName = s;
        this.dob = of;
        this.address = add;
        this.phone = s1;
        this.email = s2;
        this.password = pass;
    }

    // Implementing UserDetails methods
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // You can map roles here, returning a list of authorities.
        return List.of(() -> "ROLE_CANDIDATE");
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Customize based on your logic
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Customize based on your logic
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Customize based on your logic
    }

    @Override
    public boolean isEnabled() {
        return true; // Customize based on your logic
    }
}
