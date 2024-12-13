package vn.edu.iuh.fit.week05.frontend.controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.edu.iuh.fit.week05.backend.models.*;
import vn.edu.iuh.fit.week05.backend.repositories.JobRepository;
import vn.edu.iuh.fit.week05.backend.services.JobService;
import vn.edu.iuh.fit.week05.backend.services.JobSkillService;
import vn.edu.iuh.fit.week05.backend.services.SkillService;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/company")
public class CompanyController {
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private JobService jobService;
    @Autowired
    private SkillService skillService;
    @Autowired
    private JobSkillService jobSkillService;


    public CompanyController(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @GetMapping("/home")
    public String companyHomePage(HttpSession session, Model model) {
        Company company = (Company) session.getAttribute("user");
        model.addAttribute("company", company);

        List<Job> jobList = jobRepository.findByCompanyId(company.getId());
        model.addAttribute("listJob", jobList);

        return "/company/home";
    }


    @GetMapping("/add-job")
    public String addJob(HttpSession session, Model model) {
        Company company = (Company) session.getAttribute("user");
        model.addAttribute("company", company);
        model.addAttribute("availableSkills", skillService.getAllSkills());


        return "/company/add-job";
    }

    @PostMapping("/add-job")
    public String addJob(@RequestParam String jobName,
                         @RequestParam String jobDescription,
                         @RequestParam Map<String, String> allParams, HttpSession session) {
        Company company = (Company) session.getAttribute("user");
        if (company == null) {
            return "redirect:/login";
        }

        // Save Job
        Job job = new Job();
        job.setName(jobName);
        job.setDescription(jobDescription);
        job.setCompany(company);
        Job savedJob = jobService.saveJob(job); // Save the job

        // Parse Skills
        int i = 0;
        while (allParams.containsKey("skills[" + i + "].skill")) {
            String skillId = allParams.get("skills[" + i + "].skill");
            String skillLevel = allParams.get("skills[" + i + "].skillLevel");
            String moreInfos = allParams.get("skills[" + i + "].moreInfos");

            Skill skill = skillService.getSkillById(Long.parseLong(skillId));
            JobSkill jobSkill = new JobSkill();

            JobSkillId jobSkillId = new JobSkillId(savedJob.getId(), Long.parseLong(skillId));
            jobSkill.setId(jobSkillId); // Set JobSkillId

            jobSkill.setJob(savedJob);  // Set job
            jobSkill.setSkill(skill);   // Set skill
            jobSkill.setSkillLevel(SkillLevel.fromValue(Integer.parseInt(skillLevel)));
            jobSkill.setMoreInfos(moreInfos);

            jobSkillService.save(jobSkill); // Save JobSkill

            i++;
        }

        return "redirect:/company/home"; // Adjust as needed
    }

    @GetMapping("/edit-job")
    public String editJob(@RequestParam Long jobId, HttpSession session, Model model) {
        Company company = (Company) session.getAttribute("user");
        if (company == null) {
            return "redirect:/login";
        }

        Job job = jobService.getJobById(jobId);
        if (job == null || !job.getCompany().getId().equals(company.getId())) {
            return "redirect:/company/home"; // Không cho phép chỉnh sửa
        }

        model.addAttribute("job", job);
        model.addAttribute("availableSkills", skillService.getAllSkills());

        return "/company/edit-job";
    }

    @PostMapping("/edit-job")
    public String editJob(@RequestParam Long jobId,
                          @RequestParam String jobName,
                          @RequestParam String jobDescription,
                          @RequestParam Map<String, String> allParams,
                          HttpSession session) {
        Company company = (Company) session.getAttribute("user");
        if (company == null) {
            return "redirect:/login";
        }

        Job job = jobService.getJobById(jobId);
        if (job == null || !job.getCompany().getId().equals(company.getId())) {
            return "redirect:/company/home";
        }

        // Cập nhật thông tin công việc
        job.setName(jobName);
        job.setDescription(jobDescription);
        Job updatedJob = jobService.saveJob(job);

        // Xử lý cập nhật các kỹ năng tương tự như ở phương thức thêm job
//        jobSkillService.deleteSkillsByJobId(jobId); // Xóa các kỹ năng cũ trước
        int i = 0;
        while (allParams.containsKey("skills[" + i + "].skill")) {
            String skillId = allParams.get("skills[" + i + "].skill");
            String skillLevel = allParams.get("skills[" + i + "].skillLevel");
            String moreInfos = allParams.get("skills[" + i + "].moreInfos");

            Skill skill = skillService.getSkillById(Long.parseLong(skillId));
            JobSkill jobSkill = new JobSkill();

            JobSkillId jobSkillId = new JobSkillId(updatedJob.getId(), Long.parseLong(skillId));
            jobSkill.setId(jobSkillId);
            jobSkill.setJob(updatedJob);
            jobSkill.setSkill(skill);
            jobSkill.setSkillLevel(SkillLevel.fromValue(Integer.parseInt(skillLevel)));
            jobSkill.setMoreInfos(moreInfos);

            jobSkillService.save(jobSkill);
            i++;
        }

        return "redirect:/company/home";
    }





}