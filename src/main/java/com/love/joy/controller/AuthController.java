package com.love.joy.controller;

import com.love.joy.model.PasswordResetToken;
import com.love.joy.model.User;
import com.love.joy.repository.PasswordResetTokenRepository;
import com.love.joy.service.EmailService;
import com.love.joy.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Controller
public class AuthController
{
    // controller for login, register, forgot password, verifcation
    private final UserService userService;
    private final EmailService emailService;
    private final PasswordResetTokenRepository tokenRepo;

    // base URL so i can build links inside emails
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public AuthController(UserService userService,
                          EmailService emailService,
                          PasswordResetTokenRepository tokenRepo) {
        this.userService = userService;
        this.emailService = emailService;
        this.tokenRepo = tokenRepo;
    }


    @GetMapping("/login")
    public String login(Model model, HttpServletRequest request)
    {
        // CSRF
        CsrfToken token = (CsrfToken) request.getAttribute("_csrf");
        model.addAttribute("_csrf", token);

        return "login";
    }

    // for registering

    @GetMapping("/register")
    public String registerForm()
    {
        //to show registration form
        return "register";
    }

    @PostMapping("/register")
    public String doRegister(@RequestParam String email,
                             @RequestParam String password,
                             @RequestParam String confirmPassword,
                             @RequestParam String name,
                             @RequestParam(required = false) String phone,
                             @RequestParam String securityQuestion,
                             @RequestParam String securityAnswer,
                             Model model) {
        try {
            // to makwe sure email and password are not empty
            if (!StringUtils.hasText(email) || !StringUtils.hasText(password))
            {
                model.addAttribute("error", "Email and password required");
                return "register";
            }
            // to make sure password fields are matching
            if (!password.equals(confirmPassword))
            {
                model.addAttribute("error", "Passwords do not match");
                return "register";
            }
            // to make sure user picks a security q and answer
            if (!StringUtils.hasText(securityQuestion) || !StringUtils.hasText(securityAnswer))
            {
                model.addAttribute("error", "Security question and answer are required");
                return "register";
            }

            // creating the user  password policy roles and security qs
            User u = userService.registerNewUser(
                    email,
                    password,
                    name,
                    phone,
                    securityQuestion,
                    securityAnswer
            );

            // send verification token
            //making an email verification token so the account must be confirmed first
            String token = UUID.randomUUID().toString();
            PasswordResetToken prt = new PasswordResetToken();
            prt.setToken(token);
            prt.setUser(u);
            prt.setExpiresAt(LocalDateTime.now().plusDays(1));
            tokenRepo.save(prt);

            //makew verification link and send it by email using my EmailService
            String link = baseUrl + "/verify?token=" + token;
            emailService.sendingMessage(
                    email,
                    "Verify your Lovejoy account",
                    "Click to verify: " + link
            );

            //show message to make sure user verifies email first here instead of redirecting to login directly
            model.addAttribute("message",
                    "Registered :) Please check your email to verify the account.");
            return "message";

            // for errors return to the register page and show error message
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            return "register";
        }
    }

    //for verifying account

    @GetMapping("/verify")
    public String verify(@RequestParam String token, Model model)
    {
        //finding verification token in password_reset_tokens table
        PasswordResetToken t = tokenRepo.findById(token).orElse(null);
        // making sure token exists and not expired
        if (t == null || t.getExpiresAt().isBefore(LocalDateTime.now()))
        {
            model.addAttribute("error", "Invalid or expired token");
            return "message";
        }
        //token is valid then enable this user
        User u = t.getUser();
        u.setEnabled(true);
        userService.enableUser(u);

        // delete token so it cant be used again
        tokenRepo.delete(t);

        //return message account verified
        model.addAttribute("message", "Account verified :)  You can now log in.");
        return "message";
    }

    //now for forget password

    // first ask for users email
    @GetMapping("/forgot-password")
    public String forgotForm()
    {
        // showing forgot password type email
        return "forgot_password";
    }

    //then show the users chosen security question
    @PostMapping("/forgot-password")
    public String showSecurityQuestion(@RequestParam String email, Model model)
    {
        // look up user by email
        User u = userService.findByEmail(email).orElse(null);

        // if user not found or don't have a security question
        if (u == null || u.getSecurityQuestion() == null)
        {
            // return simple message that doesnt reveal whether email exists
            model.addAttribute("message", "If this email exists you'll receive a reset link.");
            return "message";
        }

        // if user exists then show their security question on the next page
        model.addAttribute("email", u.getEmail());
        model.addAttribute("securityQuestion", u.getSecurityQuestion());
        return "forgot_password_question";
    }

    //check answer and send reset link
    @PostMapping("/forgot-password/question")
    public String sendResetLink(@RequestParam String email,
                                @RequestParam String securityAnswer,
                                Model model) {

        //looking up user again by email
        User u = userService.findByEmail(email).orElse(null);
        if (u == null)
        {
            model.addAttribute("message", "If this email exists you'll receive a reset link.");
            return "message";
        }

        //checking answer before sending reset link
        if (!userService.verifySecurityAnswer(u, securityAnswer))
        {
            // if the answer is wrong then keep the user on the question page and show error
            model.addAttribute("email", u.getEmail());
            model.addAttribute("securityQuestion", u.getSecurityQuestion());
            model.addAttribute("error", "Security answer is incorrect");
            return "forgot_password_question";
        }

        // if answer is correct then makepassword reset token and send email
        String token = UUID.randomUUID().toString();
        PasswordResetToken prt = new PasswordResetToken();
        prt.setToken(token);
        prt.setUser(u);
        prt.setExpiresAt(LocalDateTime.now().plusHours(2)); //reset link valid for 2 hours only
        tokenRepo.save(prt);

        //make password reset link and send it by email
        String link = baseUrl + "/reset-password?token=" + token;
        emailService.sendingMessage(email, "Password reset", "Reset link: " + link);

        model.addAttribute("message", "If this email exists you'll receive a reset link.");
        return "message";
    }

    //to reset password

    @GetMapping("/reset-password")
    public String resetForm(@RequestParam String token, Model model)
    {
        // passing token into the form so i know which reset request im completing
        model.addAttribute("token", token);
        return "reset_password";
    }

    @PostMapping("/reset-password")
    public String doReset(@RequestParam String token,
                          @RequestParam String password,
                          @RequestParam String confirmPassword,
                          Model model)
    {
        // validate the reset token
        PasswordResetToken prt = tokenRepo.findById(token).orElse(null);
        if (prt == null || prt.getExpiresAt().isBefore(LocalDateTime.now()))
        {
            // if invalid or expired then error and dont reset password
            model.addAttribute("error", "Invalid/expired token");
            return "message";
        }
        //making sure passwords match else return error and send back to form
        if (!password.equals(confirmPassword))
        {
            model.addAttribute("error", "Passwords do not match");
            model.addAttribute("token", token);
            return "reset_password";
        }
        //if token is valid and the passwords match then update users password using BCrypt hashing
        User u = prt.getUser();
        u.setPasswordHash(
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder(11)
                        .encode(password)
        );
        // if user was not enabled before maek sure to enable them now as well
        u.setEnabled(true);
        userService.enableUser(u);
        //delete the reset token
        tokenRepo.delete(prt);

        //return message Password reset :) to login wiht their new password
        model.addAttribute("message", "Password reset :)  Please login.");
        return "message";
    }
}