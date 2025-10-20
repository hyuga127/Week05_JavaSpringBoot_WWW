package vn.edu.iuh.fit.week05.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.week05.backend.models.JobSkill;
import vn.edu.iuh.fit.week05.backend.models.JobSkillId;
import vn.edu.iuh.fit.week05.backend.repositories.JobSkillRepository;

import java.util.List;
import java.util.Optional;

@Service
public class JobSkillService {

    private final JobSkillRepository jobSkillRepository;

    public JobSkillService(JobSkillRepository jobSkillRepository) {
        this.jobSkillRepository = jobSkillRepository;
    }

    public void save(JobSkill jobSkill) {
        jobSkillRepository.save(jobSkill);
    }

    public void delete(JobSkill jobSkill) {
        jobSkillRepository.delete(jobSkill);
    }

    @Transactional
    public void deleteByJobId(Long jobId) {
        jobSkillRepository.deleteJobSkillByJob_Id(jobId);
    }

    public List<JobSkill> findJobSkillByJobId(Long jobId) {
        List<JobSkill> jobSkills = jobSkillRepository.findJobSkillByJobId(jobId);
        if (jobSkills.isEmpty()) {
            throw new RuntimeException("No JobSkills found for Job ID: " + jobId);
        }
        return jobSkills;
    }

    public Optional<JobSkill> findById(JobSkillId id) {
        return jobSkillRepository.findById(id);
    }
}
