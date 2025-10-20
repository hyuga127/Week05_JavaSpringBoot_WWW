package vn.edu.iuh.fit.week05.frontend.controllers;

import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.iuh.fit.week05.backend.models.*;
import vn.edu.iuh.fit.week05.backend.services.ApplicationService;

import java.util.List;
import java.util.Map;

/**
 * Controller xử lý các chức năng liên quan đến Application
 * - Candidate: apply job, withdraw, xem danh sách applications, thống kê
 * - Company: xem danh sách applicants, update status
 */
@Controller
@RequestMapping("/applications")
public class ApplicationController {

    private static final Logger log = LoggerFactory.getLogger(ApplicationController.class);

    @Autowired
    private ApplicationService applicationService;

    /**
     * Candidate apply job
     * POST /applications/apply?jobId=xxx
     */
    @PostMapping("/apply")
    public String applyJob(@RequestParam Long jobId, 
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        
        Object userObj = session.getAttribute("user");
        
        // Kiểm tra xem user có phải là Candidate không
        if (!(userObj instanceof Candidate)) {
            redirectAttributes.addFlashAttribute("error", "Only candidates can apply for jobs");
            return "redirect:/login";
        }

        Candidate candidate = (Candidate) userObj;

        try {
            applicationService.applyJob(candidate.getId(), jobId);
            redirectAttributes.addFlashAttribute("success", "Application submitted successfully!");
            log.info("Candidate {} applied for job {}", candidate.getId(), jobId);
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            log.warn("Candidate {} failed to apply for job {}: {}", candidate.getId(), jobId, e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to submit application: " + e.getMessage());
            log.error("Error applying for job", e);
        }

        return "redirect:/job/" + jobId;
    }

    /**
     * Candidate withdraw application
     * POST /applications/{id}/withdraw
     */
    @PostMapping("/{id}/withdraw")
    public String withdrawApplication(@PathVariable Long id,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        
        Object userObj = session.getAttribute("user");
        
        if (!(userObj instanceof Candidate)) {
            redirectAttributes.addFlashAttribute("error", "Only candidates can withdraw applications");
            return "redirect:/login";
        }

        Candidate candidate = (Candidate) userObj;

        try {
            applicationService.withdrawApplication(id, candidate.getId());
            redirectAttributes.addFlashAttribute("success", "Application withdrawn successfully");
            log.info("Candidate {} withdrew application {}", candidate.getId(), id);
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            log.warn("Candidate {} failed to withdraw application {}: {}", candidate.getId(), id, e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to withdraw application: " + e.getMessage());
            log.error("Error withdrawing application", e);
        }

        return "redirect:/applications/my-applications";
    }

    /**
     * Candidate xem danh sách applications của mình
     * GET /applications/my-applications
     */
    @GetMapping("/my-applications")
    public String viewMyApplications(HttpSession session, Model model) {
        
        Object userObj = session.getAttribute("user");
        
        if (!(userObj instanceof Candidate)) {
            return "redirect:/login";
        }

        Candidate candidate = (Candidate) userObj;

        // Lấy danh sách applications
        List<Application> applications = applicationService.getCandidateApplications(candidate.getId());

        // Lấy thống kê
        Map<ApplicationStatus, Long> statistics = applicationService.getCandidateStatistics(candidate.getId());
        long totalApplications = applicationService.countCandidateApplications(candidate.getId());

        model.addAttribute("candidate", candidate);
        model.addAttribute("applications", applications);
        model.addAttribute("statistics", statistics);
        model.addAttribute("totalApplications", totalApplications);
        model.addAttribute("statuses", ApplicationStatus.values());

        log.info("Candidate {} viewing applications. Total: {}", candidate.getId(), totalApplications);

        return "candidate/applications";
    }

    /**
     * Company update status của application
     * POST /applications/{id}/update-status
     */
    @PostMapping("/{id}/update-status")
    public String updateApplicationStatus(@PathVariable Long id,
                                         @RequestParam ApplicationStatus status,
                                         HttpSession session,
                                         RedirectAttributes redirectAttributes) {
        
        Object userObj = session.getAttribute("user");
        
        if (!(userObj instanceof Company)) {
            redirectAttributes.addFlashAttribute("error", "Only companies can update application status");
            return "redirect:/login";
        }

        Company company = (Company) userObj;

        try {
            Application application = applicationService.updateApplicationStatus(id, company.getId(), status);
            redirectAttributes.addFlashAttribute("success", "Application status updated successfully");
            log.info("Company {} updated application {} to status {}", company.getId(), id, status);
            
            // Redirect về trang job applicants
            return "redirect:/company/job/" + application.getJob().getId() + "/applicants";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            log.warn("Company {} failed to update application {}: {}", company.getId(), id, e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update application status: " + e.getMessage());
            log.error("Error updating application status", e);
        }

        return "redirect:/company/home";
    }
}

