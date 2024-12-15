package vn.edu.iuh.fit.week05.backend.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.week05.backend.models.Job;
import vn.edu.iuh.fit.week05.backend.models.Skill;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    Page<Job> findByCompanyId(Long companyId, Pageable pageable);

    Job findByIdAndCompanyId(Long id, Long companyId);

    @Query("""
                SELECT DISTINCT j
                FROM Job j
                JOIN JobSkill js ON j.id = js.id.jobId
                JOIN CandidateSkill cs ON js.id.skillId = cs.id.skillId
                WHERE cs.id.canId = :candidateId
            """)
    Page<Job> findMatchingJobsByCandidateId(@Param("candidateId") Long candidateId, Pageable pageable);

    @Query("""
                SELECT j
                FROM Job j
                JOIN JobSkill js ON j.id = js.id.jobId
                WHERE js.id.skillId = :skillId
            """)
    List<Job> getJobBySkill(@Param("skillId") Long skillId);
}
