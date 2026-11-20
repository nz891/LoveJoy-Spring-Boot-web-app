package com.love.joy.security;

import com.love.joy.model.User;
import com.love.joy.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class LoginFailed extends SimpleUrlAuthenticationFailureHandler
{

    private final UserRepository userRepository;

    // max failed attempts before lock
    private static final int MAX_ATTEMPTS = 3;

    public LoginFailed(UserRepository userRepository)
    {
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        org.springframework.security.core.AuthenticationException exception)
            throws IOException, ServletException {

        String email = request.getParameter("username"); // from login form
        boolean isLockedNow = false;

        // if user exists then update their failed attempt counter
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent())
        {
            User user = userOpt.get();

            //only increment if not already locked
            if (!user.isAccountLocked())
            {
                user.incrementFailedAttempts(MAX_ATTEMPTS);
                userRepository.save(user);
            }

            //check after saving if they are now locked
            isLockedNow = user.isAccountLocked();
        }

        //choose URL based on lock state
        if (isLockedNow)
        {
            setDefaultFailureUrl("/login?error&locked");
        } else {
            setDefaultFailureUrl("/login?error");
        }

        super.onAuthenticationFailure(request, response, exception);
    }
}