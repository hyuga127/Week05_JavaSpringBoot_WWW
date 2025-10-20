package vn.edu.iuh.fit.week05.backend.services;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.week05.backend.models.Candidate;
import vn.edu.iuh.fit.week05.backend.repositories.CandidateRepository;

@Service
public class CandidateService {

    private final CandidateRepository candidateRepository;
    private final PasswordEncoder passwordEncoder;

    public CandidateService(CandidateRepository candidateRepository, PasswordEncoder passwordEncoder) {
        this.candidateRepository = candidateRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Page<Candidate> findAll(int pageNo, int pageSize, String sortBy, String sortDirection ) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        return candidateRepository.findAll(pageable);
    }

    public Candidate findById(Long id) {
        return candidateRepository.findById(id).orElse(null);
    }

    public Candidate save(Candidate candidate) {
        return candidateRepository.save(candidate);
    }

    public Candidate updateCandidate(Long id, Candidate updatedCandidate) {
        Candidate existingCandidate = findById(id);
        if (existingCandidate == null) {
            throw new RuntimeException("Candidate not found with id: " + id);
        }

        // Update fields
        existingCandidate.setFullName(updatedCandidate.getFullName());
        existingCandidate.setPhone(updatedCandidate.getPhone());
        existingCandidate.setDob(updatedCandidate.getDob());
        
        if (updatedCandidate.getAvatarUrl() != null) {
            existingCandidate.setAvatarUrl(updatedCandidate.getAvatarUrl());
        }
        
        if (updatedCandidate.getPassword() != null && !updatedCandidate.getPassword().isEmpty()) {
            // Update only if the provided raw password does NOT match the current hash
            if (!passwordEncoder.matches(updatedCandidate.getPassword(), existingCandidate.getPassword())) {
                existingCandidate.setPassword(passwordEncoder.encode(updatedCandidate.getPassword()));
            }
        }

        return candidateRepository.save(existingCandidate);
    }

    public Candidate updateAvatar(Long id, String avatarUrl) {
        Candidate candidate = findById(id);
        if (candidate == null) {
            throw new RuntimeException("Candidate not found with id: " + id);
        }
        candidate.setAvatarUrl(avatarUrl);
        return candidateRepository.save(candidate);
    }



}
