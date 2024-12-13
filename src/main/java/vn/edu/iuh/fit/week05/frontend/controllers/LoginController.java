package vn.edu.iuh.fit.week05.frontend.controllers;

import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.edu.iuh.fit.week05.backend.models.Candidate;
import vn.edu.iuh.fit.week05.backend.models.Company;

@Controller
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @GetMapping("/signin")
    public String signin(HttpSession session, Model model) {
        // Kiểm tra và truyền error từ session sang Model3
        String error = (String) session.getAttribute("error");
        if (error != null) {
            model.addAttribute("error", error);
            session.removeAttribute("error"); // Xóa error sau khi đã hiển thị
        }
        return "login"; // Tên file HTML trong thư mục templates
    }

    @GetMapping("/logout")
    public String logout() {
        return "login";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }
}