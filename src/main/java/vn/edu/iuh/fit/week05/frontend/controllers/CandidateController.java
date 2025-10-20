package vn.edu.iuh.fit.week05.frontend.controllers;

import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.week05.backend.models.*;
import vn.edu.iuh.fit.week05.backend.services.*;
import vn.edu.iuh.fit.week05.frontend.utils.Greeting;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.IntStream;


@Controller
@RequestMapping("/candidate")
public class CandidateController {

    private static final Logger log = LoggerFactory.getLogger(CandidateController.class);
    @Autowired
    private CandidateService candidateService;
    @Autowired
    private JobService jobService;
    @Autowired
    private CandidateSkillService candidateSkillService;
    @Autowired
    private SkillService skillService;
    @Autowired
    private vn.edu.iuh.fit.week05.backend.services.FileStorageService fileStorageService;


    @GetMapping("/home")
    public String candidateHomePage(
            HttpSession session,
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        // Lấy thông tin candidate từ session
        Candidate candidate = (Candidate) session.getAttribute("user");
        if (candidate == null) {
            return "redirect:/login"; // Nếu không có candidate trong session, chuyển hướng đến trang đăng nhập
        }

        // Thêm thông tin candidate và lời chào vào model
        model.addAttribute("candidate", candidate);
        model.addAttribute("greeting", Greeting.getGreeting());

        // Sử dụng JobRepository để tìm các công việc khớp với kỹ năng của candidate
        Pageable pageable = PageRequest.of(page, size);
        Page<Job> jobPage = jobService.findMatchingJobsByCandidateId(candidate.getId(), pageable);

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

        // Thêm vào model
        model.addAttribute("jobPage", jobPage); // Chứa dữ liệu công việc và phân trang

        return "/candidate/home";
    }


    @Autowired
    private JobSkillService jobSkillService;


    @GetMapping("/candidates-paging")
    public String showCandidateListPaging(Model model, @RequestParam("page") Optional<Integer> page,
                                          @RequestParam("size") Optional<Integer> size) {
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(10);

        Page<Candidate> candidatePage = candidateService.findAll(currentPage - 1, pageSize, "id", "asc");
        model.addAttribute("candidatePage", candidatePage);

        int totalPages = candidatePage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .toList();
            model.addAttribute("pageNumbers", pageNumbers);
        }
        return "/candidate/candidates-paging";
    }

    @GetMapping("/edit-information")
    public String editInformation(HttpSession session, Model model) {
        Candidate candidate = (Candidate) session.getAttribute("user");
        log.info("Candidate: {}", candidate);

//        if (candidate == null) {
//            return "redirect:/login";
//        }
        model.addAttribute("candidate", candidate);
//        // Ensure this path matches the template location
        return "candidate/edit-information";
    }

//    @GetMapping("/update-skills")
//    public String updateSkills(HttpSession session, Model model) {
//        Candidate candidate = (Candidate) session.getAttribute("user");
//        model.addAttribute("candidate", candidate);
//
//        List<CandidateSkill> skills = candidateSkillService.findCandidateSkillByCandidateId(candidate.getId());
//
//        List<Skill> allSkills = skillService.getAllSkills();
//
//        model.addAttribute("candidateSkills", skills);
//        model.addAttribute("allSkills", allSkills);
//
//
//        return "candidate/update-skills";
//    }

    @GetMapping("/update-skills")
    public String updateSkills(HttpSession session, Model model) {
        Candidate candidate = (Candidate) session.getAttribute("user");
        model.addAttribute("candidate", candidate);

        List<CandidateSkill> skills = candidateSkillService.findCandidateSkillByCandidateId(candidate.getId());
        model.addAttribute("skills", skills);
        List<Skill> availableSkills = skillService.getAllSkills();
        model.addAttribute("availableSkills", availableSkills);

        return "candidate/update-skills";
    }

    @PostMapping("/update-skills")
    public String updateSkills(@RequestParam Long candidateId, @RequestParam Map<String, String> params) {
        try {
            // Nạp Candidate từ cơ sở dữ liệu
            Candidate candidate = candidateService.findById(candidateId);
            List<CandidateSkill> canSkillsBefore = candidateSkillService.findCandidateSkillByCandidateId(candidateId);

            for (CandidateSkill canSkill : canSkillsBefore) {
                System.out.println("Skill id" + canSkill.getId().getSkillId());
                System.out.println("Skill level" + canSkill.getSkillLevel().getName());
                System.out.println("More infos" + canSkill.getMoreInfos());
            }

            // Lấy danh sách kỹ năng hiện tại của candidate
            List<CandidateSkill> canSkillAfter = new ArrayList<>();

            int i = 0;
            while (params.containsKey("skills[" + i + "].id")) {
                String skillIdStr = params.get("skills[" + i + "].id");
                String skillLevelStr = params.get("skills[" + i + "].skillLevel");
                String moreInfos = params.get("skills[" + i + "].moreInfos");

                if (skillIdStr == null || skillIdStr.isEmpty() || skillLevelStr == null || skillLevelStr.isEmpty()) {
                    i++;
                    continue; // Bỏ qua nếu thiếu thông tin quan trọng
                }

                Long skillId = Long.parseLong(skillIdStr);
                SkillLevel skillLevel = SkillLevel.fromValue(Integer.parseInt(skillLevelStr));
                CandidateSkill candidateAdd = new CandidateSkill();
                candidateAdd.setId(new CandidateSkillId(skillId, candidateId));
                candidateAdd.setSkillLevel(skillLevel);
                candidateAdd.setMoreInfos(moreInfos);

                canSkillAfter.add(candidateAdd);

                // Kiểm tra xem CandidateSkill đã tồn tại chưa
                CandidateSkillId candidateSkillId = new CandidateSkillId(skillId, candidateId);
                Optional<CandidateSkill> existingCandidateSkill = candidateSkillService.findById(candidateSkillId);

                CandidateSkill candidateSkill;
                if (existingCandidateSkill.isPresent()) {
                    // Bản ghi đã tồn tại -> cập nhật
                    candidateSkill = existingCandidateSkill.get();
                    candidateSkill.setSkillLevel(skillLevel);
                    candidateSkill.setMoreInfos(moreInfos);
                } else {
                    // Bản ghi chưa tồn tại -> thêm mới
                    candidateSkill = new CandidateSkill(candidateSkillId);
                    candidateSkill.setCandidate(candidate);
                    candidateSkill.setSkill(skillService.getSkillById(skillId));
                    candidateSkill.setSkillLevel(skillLevel);
                    candidateSkill.setMoreInfos(moreInfos);
                }

                // Lưu CandidateSkill
                candidateSkillService.save(candidateSkill);

                i++;
            }

            // Xóa các CandidateSkill không còn tồn tại
            for (CandidateSkill canSkill : canSkillsBefore) {
                if (canSkillAfter.stream().noneMatch(s -> s.getId().getSkillId().equals(canSkill.getId().getSkillId()))) {
                    candidateSkillService.delete(canSkill);
                }
            }

        } catch (Exception e) {
            System.err.println("Error updating skills: " + e.getMessage());
            return "redirect:/error?message=Error updating skills.";
        }

        return "redirect:/candidate/home";
    }

    @GetMapping("/suggest-skills")
    public String getSkillSuggestions(HttpSession session,Model model) {
        Candidate candidate = (Candidate) session.getAttribute("user");
        Long candidateId = candidate.getId();

        // Lấy danh sách các kỹ năng mà candidate chưa học
        List<Skill> suggestedSkills = skillService.getSuggestedSkills(candidateId);

        // Lấy danh sách các công việc liên quan đến từng kỹ năng
        Map<String, List<Job>> jobsBySkill = new HashMap<>();
        for (Skill skill : suggestedSkills) {
            jobsBySkill.put(skill.getSkillName(), jobService.getJobBySkill(skill.getId()));
        }

        model.addAttribute("suggestedSkills", suggestedSkills);
        model.addAttribute("jobsBySkill", jobsBySkill);

        return "/candidate/skill-suggestions"; // Trang hiển thị danh sách kỹ năng và công việc
    }

    @GetMapping("/profile")
    public String viewProfile(HttpSession session, Model model) {
        Candidate candidate = (Candidate) session.getAttribute("user");
        if (candidate == null) {
            return "redirect:/login";
        }
        
        // Refresh candidate data from database
        Candidate refreshedCandidate = candidateService.findById(candidate.getId());
        model.addAttribute("candidate", refreshedCandidate);
        
        // Get candidate skills
        List<CandidateSkill> skills = candidateSkillService.findCandidateSkillByCandidateId(refreshedCandidate.getId());
        model.addAttribute("skills", skills);
        
        return "candidate/profile";
    }

    @PostMapping("/update-profile")
    public String updateProfile(
            HttpSession session,
            @RequestParam String fullName,
            @RequestParam String phone,
            @RequestParam String dob,
            @RequestParam(required = false) String password,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar,
            RedirectAttributes redirectAttributes) {
        
        Candidate candidate = (Candidate) session.getAttribute("user");
        if (candidate == null) {
            return "redirect:/login";
        }

        try {
            Candidate updatedCandidate = candidateService.findById(candidate.getId());
            updatedCandidate.setFullName(fullName);
            updatedCandidate.setPhone(phone);
            updatedCandidate.setDob(java.time.LocalDate.parse(dob));
            
            // Handle avatar upload
            if (avatar != null && !avatar.isEmpty()) {
                // Delete old avatar if exists
                if (updatedCandidate.getAvatarUrl() != null) {
                    fileStorageService.deleteFile(updatedCandidate.getAvatarUrl());
                }
                // Store new avatar
                String avatarUrl = fileStorageService.storeFile(avatar);
                updatedCandidate.setAvatarUrl(avatarUrl);
            }
            
            // Update password only if provided; ensure null when blank to avoid accidental updates
            if (password != null && !password.trim().isEmpty()) {
                updatedCandidate.setPassword(password);
            } else {
                updatedCandidate.setPassword(null);
            }
            
            Candidate saved = candidateService.updateCandidate(candidate.getId(), updatedCandidate);
            
            // Update session with fresh entity from DB
            session.setAttribute("user", saved);
            
            redirectAttributes.addFlashAttribute("message", "Profile updated successfully!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Error updating profile: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
        }

        return "redirect:/candidate/profile";
    }

}


