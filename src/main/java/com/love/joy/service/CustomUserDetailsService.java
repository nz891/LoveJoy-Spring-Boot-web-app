package com.love.joy.service;

import com.love.joy.model.Role;
import com.love.joy.model.User;
import com.love.joy.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;
import java.util.Set;

@Service
public class CustomUserDetailsService implements UserDetailsService
{

    @Autowired
    private UserRepository userRepository;

    // my Google reCAPTCHA checker
    @Autowired
    private RecaptchaService recaptchaService;

    public CustomUserDetailsService()
    {
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException
    {

        // check login captcha first (before even touching the password)
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attrs != null)
        {
            HttpServletRequest req = attrs.getRequest();

            //this is the token that comes from the Google reCAPTCHA widget in my login form
            String token = req.getParameter("g-recaptcha-response");
            String clientIp = req.getRemoteAddr();

            //if the user skips captcha or null then the login is wrong
            if (token == null || token.isBlank() || !recaptchaService.verify(token, clientIp))
            {
                // act like wrong password so I dont give any hints to attackers
                throw new BadCredentialsException("Invalid captcha");
            }
        }

        // load user from my database using the email as the username
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty())
        {
            // if there is no user with this email then user not foudn
            throw new UsernameNotFoundException("User not found :( ");
        }

        User user = userOpt.get();

        // if user has exceeded the max failed attempts thne blovk login
        if (user.isAccountLocked())
        {
            throw new LockedException("Account is locked until " + user.getLockedUntil());
        }

        // make role entities into Spring security authorities
        Set<Role> roles = user.getRoles();
        org.springframework.security.core.GrantedAuthority[] authorities = roles
                .toArray(new org.springframework.security.core.GrantedAuthority[0]);

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .disabled(!user.isEnabled())
                .authorities(authorities)
                .build();
    }
}