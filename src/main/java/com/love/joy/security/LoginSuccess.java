package com.love.joy.security;

import com.love.joy.repository.UserRepository;
import com.love.joy.service.TwoFactorAuthService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

@Component
public class LoginSuccess implements AuthenticationSuccessHandler
{

    private final UserRepository userRepository;
    private final TwoFactorAuthService twoFactorAuthService;

    public LoginSuccess(UserRepository userRepository,
                        TwoFactorAuthService twoFactorAuthService)
    {
        this.userRepository = userRepository;
        this.twoFactorAuthService = twoFactorAuthService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException {

        // getting email (username = email)
        String email = authentication.getName();

        // first finding user then reset failed attempts then start 2FA
        userRepository.findByEmail(email).ifPresent(user -> {
            user.resetFailedAttempts();          //lockout counters
            userRepository.save(user);

            // start two-factor (generating code and emailing it)
            twoFactorAuthService.startTwoFactor(user);
        });

        //store roles in session so we can redirect after 2FA
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
        request.getSession().setAttribute("ROLES_AFTER_2FA", roles);

        // sendijg user to /2fa page to enter the code
        response.sendRedirect("/2fa");
    }
}