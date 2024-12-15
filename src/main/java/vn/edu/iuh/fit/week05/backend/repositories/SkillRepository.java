package vn.edu.iuh.fit.week05.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.week05.backend.models.Skill;

import java.util.List;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {
    @Query(value = "SELECT s.* FROM skill s " +
            "WHERE s.skill_id NOT IN (" +
            "    SELECT skill_id FROM candidate_skill WHERE can_id = :candidateId" +
            ")", nativeQuery = true)
    List<Skill> findSkillNotLearnByCandidate(@Param("candidateId") Long candidateId);


}
