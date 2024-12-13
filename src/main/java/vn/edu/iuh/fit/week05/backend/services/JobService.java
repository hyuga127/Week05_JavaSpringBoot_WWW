package vn.edu.iuh.fit.week05.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.week05.backend.models.Job;
import vn.edu.iuh.fit.week05.backend.repositories.JobRepository;

import java.util.List;

@Service
public class JobService {
    @Autowired
    private JobRepository jobRepository;

    public Job getJobById(Long id) {
        return jobRepository.findById(id).orElse(null);
    }

    public Job saveJob(Job job) {
        return jobRepository.save(job);
    }

    public void deleteJob(Long id) {
        jobRepository.deleteById(id);
    }
}
