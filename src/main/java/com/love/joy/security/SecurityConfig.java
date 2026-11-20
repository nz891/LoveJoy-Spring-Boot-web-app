package com.love.joy.security;

import com.love.joy.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig
{

    private final CustomUserDetailsService customUserDetailsService;
    private final LoginSuccess loginSuccess;
    private final LoginFailed loginFailed;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService,
                          LoginSuccess loginSuccess,
                          LoginFailed loginFailed)
    {
        this.customUserDetailsService = customUserDetailsService;
        this.loginSuccess = loginSuccess;
        this.loginFailed = loginFailed;
    }

    //using BCrypt to hash all passwords in the database
    @Bean
    public BCryptPasswordEncoder passwordEncoder()
    {
        return new BCryptPasswordEncoder();
    }

    //telling spring to authenticate using my DB users and password encoder
    @Bean
    public DaoAuthenticationProvider authProvider()
    {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService); //load user by email
        provider.setPasswordEncoder(passwordEncoder()); //verifying with BCrypt hash
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception
    {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception
    {
        http
                // custom auth provider (db users and BCrypt)
                .authenticationProvider(authProvider())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/register",
                                "/login",
                                "/forgot-password",  //email
                                "/forgot-password/question", //security answer
                                "/verify", //email verification link
                                "/reset-password/**"  //password reset using token
                        ).permitAll()
                        //static files stay public so the UI loads
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        //admin pages only users with ROLE_ADMIN can see these
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        //everything else needds a logged in authenticated user
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        //my login page
                        .loginPage("/login")
                        //if login is done then run handler (reset failures, start 2FA, redirect)
                        .successHandler(loginSuccess)
                        // if login fails then run failure handler (increment attempts, lockout)
                        .failureHandler(loginFailed)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        //after logging out send user back to login page
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        //so sprin can apply everything
        return http.build();
    }
}