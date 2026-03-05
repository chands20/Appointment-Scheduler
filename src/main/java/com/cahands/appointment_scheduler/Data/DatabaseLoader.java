package com.cahands.appointment_scheduler.Data;

import com.cahands.appointment_scheduler.Model.User;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DatabaseLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseLoader(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            // Instructor 1
            User inst1 = new User();
            inst1.setName("Ms. Candace");
            inst1.setUsername("instructor1");
            inst1.setPassword(passwordEncoder.encode("instructor1"));
            inst1.setRole(User.Role.INSTRUCTOR);
            userRepository.save(inst1);
            // Instructor 2
            User inst2 = new User();
            inst2.setName("Mr. Johnson");
            inst2.setUsername("instructor2");
            inst2.setPassword(passwordEncoder.encode("instructor2"));
            inst2.setRole(User.Role.INSTRUCTOR);
            userRepository.save(inst2);

            // Student 1
            User stud1 = new User();
            stud1.setName("Jack");
            stud1.setUsername("student1");
            stud1.setPassword(passwordEncoder.encode("student1"));
            stud1.setRole(User.Role.STUDENT);
            userRepository.save(stud1);
            // Student 2
            User stud2 = new User();
            stud2.setName("Jill");
            stud2.setUsername("student2");
            stud2.setPassword(passwordEncoder.encode("student2"));
            stud2.setRole(User.Role.STUDENT);
            userRepository.save(stud2);
            // Student 3
            User stud3 = new User();
            stud3.setName("John");
            stud3.setUsername("student3");
            stud3.setPassword(passwordEncoder.encode("student3"));
            stud3.setRole(User.Role.STUDENT);
            userRepository.save(stud3);

            System.out.println("USERS IMPORTED INTO DATABASE");
        }
    }
}