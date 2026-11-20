package com.love.joy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController
{
    // if someone goes to http://localhost:8080/ with no extra path always send them to the login screen
	 @GetMapping("/")
    public String home()
     {
        return "redirect:/login";
    }
}
