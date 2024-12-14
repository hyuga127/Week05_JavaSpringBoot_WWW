package vn.edu.iuh.fit.week05.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.week05.backend.models.JobSkill;
import vn.edu.iuh.fit.week05.backend.repositories.JobSkillRepository;

import java.util.List;

@Service
public class JobSkillService {

    @Autowired
    private JobSkillRepository jobSkillRepository;

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
        return jobSkillRepository.findJobSkillByJobId(jobId);
    }
}
