package com.love.joy.controller;

import com.love.joy.repository.EvaluationRequestRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController
{
    //this repository lets the admin see all evaluation requests stored in the database
    private final EvaluationRequestRepository evalRepo;
    //here using constructor injection its the recommended way in Spring
    public AdminController(EvaluationRequestRepository evalRepo){ this.evalRepo = evalRepo; }

    // here method handles GET requests to /admin/requests
    @GetMapping("/admin/requests")
    public String listRequests(Model model)
    {
        // addingall requests so that the admin can view them in the admin_list page
        model.addAttribute("requests", evalRepo.findAll());
        //here return admin_list.html that displays the table of requests
        return "admin_list";
    }
}
