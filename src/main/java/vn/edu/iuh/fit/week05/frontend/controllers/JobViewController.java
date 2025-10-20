package vn.edu.iuh.fit.week05.frontend.controllers;

import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import vn.edu.iuh.fit.week05.backend.models.Application;
import vn.edu.iuh.fit.week05.backend.models.Candidate;
import vn.edu.iuh.fit.week05.backend.models.Job;
import vn.edu.iuh.fit.week05.backend.services.ApplicationService;
import vn.edu.iuh.fit.week05.backend.services.JobService;

import java.util.Optional;

/**
 * Controller để hiển thị các view liên quan đến Job
 */
@Controller
@RequestMapping("/job")
public class JobViewController {

    private static final Logger log = LoggerFactory.getLogger(JobViewController.class);

    @Autowired
    private JobService jobService;

    @Autowired
    private ApplicationService applicationService;

    /**
     * Hiển thị trang chi tiết job
     * GET /job/{id}
     */
    @GetMapping("/{id}")
    public String viewJobDetail(@PathVariable Long id, HttpSession session, Model model) {
        
        // Lấy job với company và jobSkills (EAGER fetch để tránh LazyInitializationException)
        Job job = jobService.getJobByIdWithDetails(id);
        if (job == null) {
            log.warn("Job not found with ID: {}", id);
            return "redirect:/candidate/home";
        }

        model.addAttribute("job", job);

        // Kiểm tra xem user có phải candidate không và đã apply chưa
        Object userObj = session.getAttribute("user");
        if (userObj instanceof Candidate) {
            Candidate candidate = (Candidate) userObj;
            
            // Kiểm tra xem đã apply chưa
            try {
                Optional<Application> existingApplication = applicationService.getApplication(candidate.getId(), id);
                log.debug("Checking application for candidate {} and job {}: {}", candidate.getId(), id, existingApplication.isPresent());
                
                if (existingApplication.isPresent()) {
                    Application app = existingApplication.get();
                    log.debug("Application found: id={}, status={}, createdDate={}", 
                            app.getId(), app.getStatus(), app.getCreatedDate());
                    model.addAttribute("hasApplied", true);
                    model.addAttribute("application", app);
                } else {
                    model.addAttribute("hasApplied", false);
                    log.debug("No application found for candidate {} and job {}", candidate.getId(), id);
                }
            } catch (Exception e) {
                log.error("Error getting application for candidate {} and job {}", candidate.getId(), id, e);
                model.addAttribute("hasApplied", false);
            }
            
            model.addAttribute("isCandidate", true);
        } else {
            model.addAttribute("hasApplied", false);
            model.addAttribute("isCandidate", false);
        }

        log.info("Viewing job detail: {}", id);

        return "job-detail";
    }
}

