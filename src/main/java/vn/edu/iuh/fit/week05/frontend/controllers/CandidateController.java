package vn.edu.iuh.fit.week05.frontend.controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.edu.iuh.fit.week05.backend.models.Candidate;
import vn.edu.iuh.fit.week05.backend.repositories.CandidateRepository;
import vn.edu.iuh.fit.week05.backend.services.CandidateService;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;


@Controller
@RequestMapping("/candidate")
public class CandidateController {

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private CandidateService candidateService;

    @GetMapping("/home")
    public String candidateHomePage(HttpSession session, Model model) {
        Candidate candidate = (Candidate) session.getAttribute("user");
        model.addAttribute("candidate", candidate);
        return "/candidate/home";
    }

    @GetMapping("/candidates-paging")
    public String showCandidateListPaging(Model model, @RequestParam("page")Optional<Integer> page,
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
}
