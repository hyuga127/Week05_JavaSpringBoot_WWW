package vn.edu.iuh.fit.week05.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.iuh.fit.week05.backend.models.CandidateSkill;
import vn.edu.iuh.fit.week05.backend.models.CandidateSkillId;

import java.util.List;


public interface CandidateSkillRepository extends JpaRepository<CandidateSkill, CandidateSkillId> {

    List<CandidateSkill> findCandidateSkillByCandidateId(Long candidateId);
}
