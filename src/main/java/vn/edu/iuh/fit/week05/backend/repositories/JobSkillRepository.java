package vn.edu.iuh.fit.week05.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.week05.backend.models.JobSkill;
import vn.edu.iuh.fit.week05.backend.models.JobSkillId;

@Repository
public interface JobSkillRepository extends JpaRepository<JobSkill, JobSkillId> {
}
