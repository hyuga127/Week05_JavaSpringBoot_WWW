package vn.edu.iuh.fit.week05.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.week05.backend.models.Candidate;
import vn.edu.iuh.fit.week05.backend.models.Company;
import vn.edu.iuh.fit.week05.backend.repositories.CandidateRepository;
import vn.edu.iuh.fit.week05.backend.repositories.CompanyRepository;

import java.util.List;
import java.util.Optional;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final CandidateRepository candidateRepository;
    private final CompanyRepository companyRepository;

    public AppUserDetailsService(CandidateRepository candidateRepository, CompanyRepository companyRepository) {
        this.candidateRepository = candidateRepository;
        this.companyRepository = companyRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // First, check if the email belongs to a Candidate
        Optional<Candidate> candidate = candidateRepository.findByEmail(email);
        if (candidate.isPresent()) {
            return new User(
                    candidate.get().getEmail(),
                    candidate.get().getPassword(),
                    List.of(new SimpleGrantedAuthority("ROLE_CANDIDATE"))
            );
        }

        // Then, check if the email belongs to a Company
        Optional<Company> company = companyRepository.findByEmail(email);
        if (company.isPresent()) {
            return new User(
                    company.get().getEmail(),
                    company.get().getPassword(),
                    List.of(new SimpleGrantedAuthority("ROLE_COMPANY"))
            );
        }

        // If no match, throw an exception
        throw new UsernameNotFoundException("User not found with email: " + email);
    }
}
