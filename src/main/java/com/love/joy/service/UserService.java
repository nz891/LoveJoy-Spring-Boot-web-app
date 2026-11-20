package com.love.joy.service;

import com.love.joy.model.Role;
import com.love.joy.model.User;
import com.love.joy.repository.RoleRepository;
import com.love.joy.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;

@Service
public class UserService
{

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final BCryptPasswordEncoder encoder;

    public UserService(UserRepository userRepo,
                       RoleRepository roleRepo,
                       BCryptPasswordEncoder encoder)
    {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.encoder = encoder;
    }

    public User registerNewUser(String email,
                                String rawPassword,
                                String name,
                                String phone,
                                String securityQuestion,
                                String securityAnswer) throws Exception {

        //make sure the email isnt already registered
        if (userRepo.findByEmail(email).isPresent())
        {
            throw new Exception("Email already in use");
        }

        //password policy: min 8, at least 1 digit and 1 special char
        if (rawPassword.length() < 8
                || !rawPassword.matches(".*\\d.*")
                || !rawPassword.matches(".*[^a-zA-Z0-9].*"))
        {
            throw new Exception(
                    "your password does not meet complexity requirements :(   " +
                            "min 8 characters, at least 1 digit and 1 special char"
            );
        }

        //new user object
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(rawPassword)); //hashing password
        u.setName(name);
        u.setPhone(phone);
        u.setEnabled(false); //false until email verified

        //storing security question and hashed answer
        u.setSecurityQuestion(securityQuestion);
        String normalizedAnswer = securityAnswer.trim().toLowerCase();
        u.setSecurityAnswerHash(encoder.encode(normalizedAnswer));

        //assign normal user role
        Role userRole = roleRepo.findByName("ROLE_USER").orElseThrow();
        u.setRoles(new HashSet<>(java.util.List.of(userRole)));

        //save user in db
        return userRepo.save(u);
    }

    //helper to find user by email
    public Optional<User> findByEmail(String email)
    {
        return userRepo.findByEmail(email);
    }

    //mark user as enabled
    public void enableUser(User user)
    {
        user.setEnabled(true);
        userRepo.save(user);
    }

    //check if security answer user typed matches hashed one stored in db
    public boolean verifySecurityAnswer(User user, String rawAnswer)
    {
        if (rawAnswer == null || user.getSecurityAnswerHash() == null)
        {
            return false;
        }
        String normalized = rawAnswer.trim().toLowerCase();
        return encoder.matches(normalized, user.getSecurityAnswerHash());
    }
}