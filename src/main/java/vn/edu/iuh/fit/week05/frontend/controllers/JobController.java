package vn.edu.iuh.fit.week05.frontend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import vn.edu.iuh.fit.week05.backend.services.JobService;
import vn.edu.iuh.fit.week05.backend.services.JobSkillService;
import vn.edu.iuh.fit.week05.backend.services.SkillService;

import java.util.List;

@Controller
public class JobController {

    @Autowired
    private JobService jobService;
    @Autowired
    private SkillService skillService;
    @Autowired
    private JobSkillService jobSkillService;


}
