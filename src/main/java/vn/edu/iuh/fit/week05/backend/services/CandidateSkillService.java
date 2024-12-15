package vn.edu.iuh.fit.week05.backend.services;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.week05.backend.models.*;
import vn.edu.iuh.fit.week05.backend.repositories.CandidateRepository;
import vn.edu.iuh.fit.week05.backend.repositories.CandidateSkillRepository;

import java.util.List;
import java.util.Optional;

@Service
public class CandidateSkillService {

    @Autowired
    private CandidateSkillRepository candidateSkillRepository;
    @Autowired
    private CandidateRepository candidateRepository;


    public void save(CandidateSkill candidateSkill) {
        candidateSkillRepository.save(candidateSkill);
    }

    public void delete(CandidateSkill candidateSkill) {
        candidateSkillRepository.delete(candidateSkill);
    }

    public List<CandidateSkill> findCandidateSkillByCandidateId(Long candidateId) {
        List<CandidateSkill> candidateSkills = candidateSkillRepository.findCandidateSkillByCandidateId(candidateId);
        if (candidateSkills.isEmpty()) {
            throw new RuntimeException("No CandidateSkills found for Candidate ID: " + candidateId);
        }
        return candidateSkills;
    }

    // Cập nhật kỹ năng cũ
    public void updateSkill(Long candidateId, Long skillId, Integer skillLevel, String moreInfos) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new EntityNotFoundException("Candidate not found"));

        CandidateSkill candidateSkill = candidate.getCandidateSkills().stream()
                .filter(s -> s.getId().getSkillId().equals(skillId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Skill not found"));

        if (skillLevel != null) candidateSkill.setSkillLevel(SkillLevel.fromValue(skillLevel));
        if (moreInfos != null) candidateSkill.setMoreInfos(moreInfos);

        candidateSkillRepository.save(candidateSkill);
    }

    public void addSkillToCandidate(Long candidateId, Long skillId, Integer skillLevel, String moreInfos) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new EntityNotFoundException("Candidate not found"));

        CandidateSkillId candidateSkillId = new CandidateSkillId(skillId, candidateId);
        CandidateSkill candidateSkill = candidateSkillRepository.findById(candidateSkillId).orElse(null);

        if (candidateSkill == null) {
            candidateSkill = new CandidateSkill(candidateSkillId, SkillLevel.fromValue(skillLevel), moreInfos);
            candidateSkill.setCandidate(candidate); // Set candidate here
            candidate.getCandidateSkills().add(candidateSkill);
            candidateSkillRepository.save(candidateSkill);
        } else {
            candidateSkill.setSkillLevel(SkillLevel.fromValue(skillLevel));
            candidateSkill.setMoreInfos(moreInfos);
            candidateSkillRepository.save(candidateSkill);
        }
    }



    public void removeSkillFromCandidate(Long candidateId, Long skillId) {
        Candidate candidate = candidateRepository.findById(candidateId
        ).orElseThrow(() -> new EntityNotFoundException("Candidate not found"));
        CandidateSkillId candidateSkillId = new CandidateSkillId(skillId, candidateId);
        Optional<CandidateSkill> candidateSkill = candidateSkillRepository.findById(candidateSkillId);
        if (candidateSkill.isPresent()) {
            candidate.getCandidateSkills().remove(candidateSkill.get());
            candidateRepository.save(candidate);
        }
    }

    public Optional<CandidateSkill> findById(CandidateSkillId candidateSkillId) {
        return candidateSkillRepository.findById(candidateSkillId);
    }


}
