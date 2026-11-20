package com.love.joy.service;

import com.love.joy.model.User;
import com.love.joy.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class TwoFactorAuthService
{

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final SecureRandom random = new SecureRandom();

    //code only valid for 5 mins
    private static final int CODE_VALID_MINUTES = 5;

    public TwoFactorAuthService(UserRepository userRepository,
                                EmailService emailService)
    {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    //generate a random 6-digit code like "482931"
    private String generateSixDigitCode()
    {
        int num = 100000 + random.nextInt(900000);
        return String.valueOf(num);
    }


     //generate a code andstore it on the user with expiry time and email the code to the user
    public void startTwoFactor(User user)
    {
        String code = generateSixDigitCode();

        user.setTwoFactorCode(code);
        user.setTwoFactorExpiresAt(LocalDateTime.now().plusMinutes(CODE_VALID_MINUTES));
        userRepository.save(user);

        //send code by email and mailHog will catch this
        String subject = "Your Lovejoy login code";
        String body = "Hi " + user.getName() + ",\n\n"
                + "Your one-time login code is: " + code + "\n"
                + "It is valid for " + CODE_VALID_MINUTES + " minutes.\n\n"
                + "If you did not try to log in, you can ignore this email.";

        emailService.sendingMessage(user.getEmail(), subject, body);
    }

    //checking if the code user typed is correct and not expired
    public boolean verifyCode(User user, String submittedCode)
    {
        //no code stored
        if (user.getTwoFactorCode() == null || user.getTwoFactorExpiresAt() == null)
        {
            return false;
        }

        //expired
        if (user.getTwoFactorExpiresAt().isBefore(LocalDateTime.now()))
        {
            return false;
        }

        //wrong code
        if (!user.getTwoFactorCode().equals(submittedCode))
        {
            return false;
        }

        //if correct and not expired then clear code and save
        user.setTwoFactorCode(null);
        user.setTwoFactorExpiresAt(null);
        userRepository.save(user);
        return true;
    }
}