package com.love.joy.controller;

import com.love.joy.model.User;
import com.love.joy.repository.UserRepository;
import com.love.joy.service.TwoFactorAuthService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.Set;

@Controller
public class TwoFactorController
{

    private final UserRepository userRepository;
    private final TwoFactorAuthService twoFactorAuthService;

    public TwoFactorController(UserRepository userRepository,
                               TwoFactorAuthService twoFactorAuthService)
    {
        this.userRepository = userRepository;
        this.twoFactorAuthService = twoFactorAuthService;
    }

    // show the 2FA form where user types one-time code
    @GetMapping("/2fa")
    public String show2faForm()
    {
        return "two_factor";
    }

    // handle 2FA form submission and decide where to send user next
    @PostMapping("/2fa")
    public String handle2fa(@RequestParam("code") String code,
                            @AuthenticationPrincipal UserDetails principal,
                            HttpSession session,
                            Model model)
    {

        if (principal == null)
        {
            // if not logged in anymore then send back to login
            return "redirect:/login";
        }

        // get the users email from spring secuiruty principle
        String email = principal.getUsername();
        User user = userRepository.findByEmail(email).orElse(null);

        //if user record doenst exist anymore thne send back to login
        if (user == null)
        {
            return "redirect:/login";
        }

        //make sure code user entered matches the one sent and not expired
        boolean ok = twoFactorAuthService.verifyCode(user, code);

        //if code is wrong or expired then stay on the 2FA page and show error
        if (!ok)
        {
            model.addAttribute("error", "Invalid or expired code. Please try again.");
            return "two_factor";
        }

        // 2FA success then send them based on role
        Set<String> roles = (Set<String>) session.getAttribute("ROLES_AFTER_2FA");

        // if user has the admin role then send them to the admin page
        if (roles != null && roles.contains("ROLE_ADMIN"))
        {
            return "redirect:/admin/requests";
        } else {
            // else send them to the evaluation form
            return "redirect:/evaluation";
        }
    }
}