package com.love.joy;

import com.love.joy.model.Role;
import com.love.joy.model.User;
import com.love.joy.repository.RoleRepository;
import com.love.joy.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.HashSet;

@Component
public class DataInitializer implements CommandLineRunner
{

    private final RoleRepository roleRepo;
    private final UserRepository userRepo;
    private final BCryptPasswordEncoder encoder;

    public DataInitializer(RoleRepository roleRepo, UserRepository userRepo, BCryptPasswordEncoder encoder)
    {
        this.roleRepo = roleRepo; this.userRepo = userRepo; this.encoder = encoder;
    }

    @Override
    public void run(String... args) throws Exception
    {
        if (roleRepo.findByName("ROLE_USER").isEmpty()) roleRepo.save(new Role(null, "ROLE_USER"));
        if (roleRepo.findByName("ROLE_ADMIN").isEmpty()) roleRepo.save(new Role(null, "ROLE_ADMIN"));

        if (userRepo.findByEmail("admin@gmail.com").isEmpty())
        {
            User admin = new User();
            admin.setEmail("admin@gmail.com");
            admin.setName("admin");
            admin.setPhone("0000000000");
            admin.setPasswordHash(encoder.encode("Admin123!")); // test password 1
            admin.setEnabled(true);
            Role adminRole = roleRepo.findByName("ROLE_ADMIN").get();
            admin.setRoles(new HashSet<>(java.util.List.of(adminRole)));
            userRepo.save(admin);
        }

        if (userRepo.findByEmail("user1@gmail.com").isEmpty())
        {
            User u = new User();
            u.setEmail("user1@gmail.com");
            u.setName("user1");
            u.setPhone("1234567890");
            u.setPasswordHash(encoder.encode("User123!")); // test password 2
            u.setEnabled(true);
            Role userRole = roleRepo.findByName("ROLE_USER").get();
            u.setRoles(new HashSet<>(java.util.List.of(userRole)));
            userRepo.save(u);
        }
    }
}
