package vn.edu.iuh.fit.week05.backend.services;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.week05.backend.models.Candidate;
import vn.edu.iuh.fit.week05.backend.models.CandidateSkill;
import vn.edu.iuh.fit.week05.backend.models.Skill;
import vn.edu.iuh.fit.week05.backend.repositories.CandidateRepository;
import vn.edu.iuh.fit.week05.backend.repositories.SkillRepository;

import java.util.List;

@Service
public class SkillService {

    private final SkillRepository skillRepository;
    private final CandidateRepository candidateRepository;

    @Autowired
    public SkillService(SkillRepository skillRepository, CandidateRepository candidateRepository) {
        this.skillRepository = skillRepository;
        this.candidateRepository = candidateRepository;
    }

    public List<Skill> getAllSkills() {
        return skillRepository.findAll();
    }

    public Skill getSkillById(Long id) {
        return skillRepository.findById(id).orElse(null);
    }

    public List<Skill> getSuggestedSkills(Long candidateId) {
        return skillRepository.findSkillNotLearnByCandidate(candidateId);
    }

    public Skill saveSkill(Skill skill) {
        return skillRepository.save(skill);
    }

    public void deleteSkill(Long id) {
        skillRepository.deleteById(id);
    }
}
