package com.cahands.appointment_scheduler.Controllers;

import com.cahands.appointment_scheduler.Data.AppointmentRepository;
import com.cahands.appointment_scheduler.Model.Appointment;
import com.cahands.appointment_scheduler.Model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/student")
public class StudentController {

    private final AppointmentRepository appointmentRepo;

    public StudentController(AppointmentRepository appointmentRepo) {
        this.appointmentRepo = appointmentRepo;
    }

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        User student = (User) session.getAttribute("loggedInUser");

        // List 1: Everything available to book
        model.addAttribute("availableAppointments", appointmentRepo.findByIsBookedFalse());

        // List 2: what this student has booked
        model.addAttribute("myBookings", appointmentRepo.findByBookedBy(student));

        return "student-dashboard";
    }

    @PostMapping("/book/{id}")
    public String bookAppointment(@PathVariable Long id, HttpSession session) {
        User student = (User) session.getAttribute("loggedInUser");
        Optional<Appointment> apptOpt = appointmentRepo.findById(id);

        if (apptOpt.isPresent()) {
            Appointment appt = apptOpt.get();
            if (!appt.isBooked()) {
                appt.setBooked(true);
                appt.setBookedBy(student); // Link the student to the slot
                appointmentRepo.save(appt);
            }
        }
        return "redirect:/student/dashboard";
    }

    @PostMapping("/cancel/{id}")
    public String cancelBooking(@PathVariable Long id, HttpSession session) {
        User student = (User) session.getAttribute("loggedInUser");
        Optional<Appointment> apptOpt = appointmentRepo.findById(id);

        if (apptOpt.isPresent()) {
            Appointment appt = apptOpt.get();
            // only allow cancellation if this student is the one who booked it
            if (appt.getBookedBy() != null && appt.getBookedBy().getId().equals(student.getId())) {
                appt.setBooked(false);
                appt.setBookedBy(null); // Remove the student link
                appointmentRepo.save(appt);
            }
        }
        return "redirect:/student/dashboard";
    }
}