package vn.edu.iuh.fit.week05.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.week05.backend.models.JobSkill;
import vn.edu.iuh.fit.week05.backend.models.JobSkillId;

import java.util.List;

@Repository
public interface JobSkillRepository extends JpaRepository<JobSkill, JobSkillId> {
    void deleteJobSkillByJob_Id(Long jobId);
    List<JobSkill> findJobSkillByJobId(Long jobId);
}
