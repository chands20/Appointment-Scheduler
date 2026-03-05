package com.cahands.appointment_scheduler.Controllers;

import com.cahands.appointment_scheduler.Data.UserRepository;
import com.cahands.appointment_scheduler.Model.User;
import jakarta.servlet.http.HttpSession;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class LoginController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // first page
    @GetMapping("/")
    public String showLoginPage() {
        return "login"; // returns login.html
    }

    @PostMapping("/login")
    public String handleLogin(@RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        Optional<User> user = userRepository.findByUsername(username);

        // user exists and password matches?
        if (user.isPresent()) {
            if (passwordEncoder.matches(password, user.get().getPassword())) {
                // user info in the session
                session.setAttribute("loggedInUser", user.get());

                // after login redirect
                if (user.get().getRole() == User.Role.INSTRUCTOR) {
                    return "redirect:/instructor/dashboard";
                } else {
                    return "redirect:/student/dashboard";
                }
            } else {
                model.addAttribute("error", "Invalid username or password");
                return "login";
            }
        }

        // If login fails
        model.addAttribute("error", "Invalid username or password");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
