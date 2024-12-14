package vn.edu.iuh.fit.week05.frontend.controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.week05.backend.models.*;
import vn.edu.iuh.fit.week05.backend.repositories.JobRepository;
import vn.edu.iuh.fit.week05.backend.services.JobService;
import vn.edu.iuh.fit.week05.backend.services.JobSkillService;
import vn.edu.iuh.fit.week05.backend.services.SkillService;

import java.util.*;
import java.util.stream.IntStream;

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
    public String companyHomePage(HttpSession session, Model model, @RequestParam(defaultValue = "0") int page) {
        Company company = (Company) session.getAttribute("user");
        if (company == null) {
            throw new IllegalStateException("Company not found in session");
        }

        // Cấu hình Pageable với kích thước cố định là 5
        Pageable pageable = PageRequest.of(page, 5); // page = số trang, 5 = số job mỗi trang

        Page<Job> jobPage = jobRepository.findByCompanyId(company.getId(), pageable);
        model.addAttribute("company", company);
        model.addAttribute("jobPage", jobPage);

        // Tạo danh sách số trang
        int totalPages = jobPage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .toList();
            model.addAttribute("pageNumbers", pageNumbers);
        } else {
            model.addAttribute("pageNumbers", List.of(1));
        }

        return "company/home";
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

    @GetMapping("/edit-job/{jobId}")
    public String editJob(@PathVariable Long jobId, HttpSession session, Model model) {
        Company company = (Company) session.getAttribute("user");

        if (company == null) {
            model.addAttribute("error", "Bạn cần đăng nhập để chỉnh sửa công việc.");
            return "redirect:/login";
        }

        Job job = jobRepository.findByIdAndCompanyId(jobId, company.getId());
        if (job == null) {
            model.addAttribute("error", "Không tìm thấy công việc.");
            return "redirect:/company/home";
        }

        List<Skill> availableSkills = skillService.getAllSkills();

        model.addAttribute("job", job);
        model.addAttribute("availableSkills", availableSkills);

        return "company/edit-job";
    }


    @PostMapping("/update-job")
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
        jobService.saveJob(job);

        // Lấy danh sách các JobSkill hiện có
        List<JobSkill> existingJobSkills = jobSkillService.findJobSkillByJobId(jobId);

        // Tạo một Map để lưu các JobSkill hiện tại
        Map<Long, JobSkill> existingJobSkillMap = new HashMap<>();
        for (JobSkill jobSkill : existingJobSkills) {
            existingJobSkillMap.put(jobSkill.getSkill().getId(), jobSkill);
        }

        // Duyệt các skill mới từ dữ liệu gửi lên
        Set<Long> processedSkillIds = new HashSet<>(); // Lưu lại các skill đã xử lý
        int i = 0;
        while (allParams.containsKey("skills[" + i + "].skill")) {
            Long skillId = Long.parseLong(allParams.get("skills[" + i + "].skill"));
            int skillLevelValue = Integer.parseInt(allParams.get("skills[" + i + "].skillLevel"));
            String moreInfos = allParams.get("skills[" + i + "].moreInfos");

            processedSkillIds.add(skillId); // Đánh dấu skill đã xử lý

            if (existingJobSkillMap.containsKey(skillId)) {
                // Cập nhật JobSkill nếu đã tồn tại
                JobSkill jobSkill = existingJobSkillMap.get(skillId);
                jobSkill.setSkillLevel(SkillLevel.fromValue(skillLevelValue));
                jobSkill.setMoreInfos(moreInfos);
                jobSkillService.save(jobSkill);
            } else {
                // Thêm JobSkill mới nếu chưa tồn tại
                Skill skill = skillService.getSkillById(skillId);
                JobSkill newJobSkill = new JobSkill();

                JobSkillId jobSkillId = new JobSkillId(jobId, skillId);
                newJobSkill.setId(jobSkillId);
                newJobSkill.setJob(job);
                newJobSkill.setSkill(skill);
                newJobSkill.setSkillLevel(SkillLevel.fromValue(skillLevelValue));
                newJobSkill.setMoreInfos(moreInfos);

                jobSkillService.save(newJobSkill);
            }
            i++;
        }

        // Xóa các JobSkill không còn trong danh sách mới (những JobSkill không có trong processedSkillIds)
        for (JobSkill jobSkillToRemove : existingJobSkillMap.values()) {
            if (!processedSkillIds.contains(jobSkillToRemove.getSkill().getId())) {
                jobSkillService.delete(jobSkillToRemove);
            }
        }

        return "redirect:/company/home";
    }







}