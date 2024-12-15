package vn.edu.iuh.fit.week05.frontend.controllers;

import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.week05.backend.models.*;
import vn.edu.iuh.fit.week05.backend.repositories.CandidateRepository;
import vn.edu.iuh.fit.week05.backend.services.CandidateService;
import vn.edu.iuh.fit.week05.backend.services.CandidateSkillService;
import vn.edu.iuh.fit.week05.backend.services.JobSkillService;
import vn.edu.iuh.fit.week05.backend.services.SkillService;
import vn.edu.iuh.fit.week05.frontend.utils.Greeting;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Controller
@RequestMapping("/candidate")
public class CandidateController {

    private static final Logger log = LoggerFactory.getLogger(CandidateController.class);
    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private CandidateService candidateService;
    @Autowired
    private SkillService skillService;

    @Autowired
    private CandidateSkillService candidateSkillService;
    @Autowired
    private JobSkillService jobSkillService;

    @GetMapping("/home")
    public String candidateHomePage(HttpSession session, Model model) {
        Candidate candidate = (Candidate) session.getAttribute("user");
        model.addAttribute("candidate", candidate);
        model.addAttribute("greeting", Greeting.getGreeting());
        return "/candidate/home";
    }

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

//    @PostMapping("/update-skills")
//    public String updateSkills(@RequestParam Long candidateId, @RequestParam Map<String, String> params) {
//        // Lấy danh sách kỹ năng hiện tại của candidate
//        Candidate candidate = candidateService.findById(candidateId);
//        Set<Long> currentSkillIds = candidate.getCandidateSkills().stream()
//                .map(s -> s.getId().getSkillId())
//                .collect(Collectors.toSet());
//
//        // Lấy danh sách kỹ năng từ request
//        Set<Long> updatedSkillIds = params.keySet().stream()
//                .filter(key -> key.matches("skills\\[\\d+]\\.id"))
//                .map(key -> Long.valueOf(params.get(key)))
//                .collect(Collectors.toSet());
//
//        // Tìm các kỹ năng cần xóa
//        Set<Long> skillsToRemove = new HashSet<>(currentSkillIds);
//        skillsToRemove.removeAll(updatedSkillIds);
//
//        // Xóa kỹ năng
//        for (Long skillId : skillsToRemove) {
//            candidateSkillService.removeSkillFromCandidate(candidateId, skillId);
//        }
//
//
//        // Phân loại kỹ năng cũ và mới
//        Map<Long, Map<String, String>> oldSkills = new HashMap<>();
//        List<Map<String, String>> newSkills = new ArrayList<>();
//
//        for (Map.Entry<String, String> entry : params.entrySet()) {
//            String key = entry.getKey();
//            String value = entry.getValue();
//
//            if (key.matches("skills\\[\\d+]\\.(id|skillLevel|moreInfos)")) {
//                // Kỹ năng cũ
//                Long skillId = Long.valueOf(key.split("\\[")[1].split("]")[0]);
//                String fieldName = key.split("\\.")[1];
//
//                oldSkills.putIfAbsent(skillId, new HashMap<>());
//                oldSkills.get(skillId).put(fieldName, value);
//
//            } else if (key.matches("skills\\[new_\\d+]\\.(id|skillLevel|moreInfos)")) {
//                // Kỹ năng mới
//                String newSkillKey = key.split("\\[")[1].split("]")[0];
//
//                Map<String, String> newSkillData = newSkills.stream()
//                        .filter(map -> map.getOrDefault("key", "").equals(newSkillKey))
//                        .findFirst()
//                        .orElseGet(() -> {
//                            Map<String, String> map = new HashMap<>();
//                            map.put("key", newSkillKey);
//                            newSkills.add(map);
//                            return map;
//                        });
//
//                String fieldName = key.split("\\.")[1];
//                newSkillData.put(fieldName, value);
//            }
//        }
//
//        // Gọi service để xử lý kỹ năng cũ
//        for (Map.Entry<Long, Map<String, String>> entry : oldSkills.entrySet()) {
//            Long skillId = entry.getKey();
//            Map<String, String> data = entry.getValue();
//
//            Integer skillLevel = data.containsKey("skillLevel") ? Integer.valueOf(data.get("skillLevel")) : null;
//            String moreInfos = data.get("moreInfos");
//
//            candidateSkillService.updateSkill(candidateId, skillId, skillLevel, moreInfos); // Gọi service cập nhật kỹ năng cũ
//        }
//
//        // Gọi service để thêm kỹ năng mới
//        for (Map<String, String> data : newSkills) {
//            Long skillId = data.containsKey("id") ? Long.valueOf(data.get("id")) : null;
//            Integer skillLevel = data.containsKey("skillLevel") ? Integer.valueOf(data.get("skillLevel")) : null;
//            String moreInfos = data.get("moreInfos");
//
//            candidateSkillService.addSkillToCandidate(candidateId, skillId, skillLevel, moreInfos); // Gọi service thêm kỹ năng mới
//        }
//
//        return "redirect:/candidate/home";
//    }

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




}


