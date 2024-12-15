package vn.edu.iuh.fit.week05.frontend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.week05.backend.models.Job;
import vn.edu.iuh.fit.week05.backend.services.JobService;

import java.io.Serializable;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/jobs")
public class JobController {

    @Autowired
    private JobService jobService;

    @GetMapping("/matching")
    public ResponseEntity<Page<Job>> getMatchingJobs(
            @RequestParam Long candidateId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Job> matchingJobs = jobService.findMatchingJobsByCandidateId(candidateId, pageable);

        return ResponseEntity.ok(matchingJobs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getJobById(@PathVariable Long id) {
        Job job = jobService.getJobById(id);
        if (job == null) {
            return ResponseEntity.notFound().build();
        }

        // Tạo danh sách jobSkills với các thông tin cần thiết
        List<Map<String, Serializable>> jobSkills = job.getJobSkills().stream()
                .map(skill -> Map.of(
                        "id", skill.getId(),
                        "skillName", skill.getSkill().getSkillName(),
                        "skillLevel", skill.getSkillLevel(), // Bổ sung skillLevel
                        "moreInfors", skill.getMoreInfos()  // Bổ sung moreInfors
                ))
                .toList();

        // Trả về dữ liệu trong một Map
        Map<String, Object> response = Map.of(
                "id", job.getId(),
                "description", job.getDescription(),
                "name", job.getName(),
                "jobSkills", jobSkills
        );

        return ResponseEntity.ok(response);
    }



}

