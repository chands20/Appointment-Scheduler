package com.cahands.appointment_scheduler.Controllers;

import com.cahands.appointment_scheduler.Data.AppointmentRepository;
import com.cahands.appointment_scheduler.Data.UserRepository;
import com.cahands.appointment_scheduler.Model.Appointment;
import com.cahands.appointment_scheduler.Model.BatchAppointmentRequest;
import com.cahands.appointment_scheduler.Model.User;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/instructor")
public class InstructorController {

    private final AppointmentRepository appointmentRepo;
    private final UserRepository userRepository;

    public InstructorController(AppointmentRepository appointmentRepo, UserRepository userRepository) {
        this.appointmentRepo = appointmentRepo;
        this.userRepository = userRepository;
    }

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        // Get the instructor from the session
        User currentInstructor = (User) session.getAttribute("loggedInUser");

        // Fetch only their appointments
        List<Appointment> myAppointments = appointmentRepo.findByInstructor(currentInstructor);

        model.addAttribute("appointments", myAppointments);
        model.addAttribute("instructorName", currentInstructor.getName());
        model.addAttribute("loggedInUser", currentInstructor);

        return "instructor-dashboard";
    }

    @GetMapping("/create-appointment")
    public String showCreateForm(Model model) {
        // pass a blank Appointment object to the form
        model.addAttribute("appointment", new Appointment());
        return "create-appointment";
    }

    @PostMapping("/create-appointment")
    public String saveAppointment(@ModelAttribute("appointment") Appointment appointment, HttpSession session,
            Model model) {

        // Get the logged-in instructor from the session
        User currentInstructor = (User) session.getAttribute("loggedInUser");

        // Checks
        if (appointment.getEndTime().isBefore(appointment.getStartTime())) {
            model.addAttribute("error", "End time cannot be before start time.");
            return "create-appointment";
        }

        if (appointment.getStartTime().isBefore(java.time.LocalDateTime.now())) {
            model.addAttribute("error", "You cannot schedule appointments in the past.");
            return "create-appointment";
        }

        List<Appointment> existing = appointmentRepo.findByInstructor(currentInstructor);
        for (Appointment other : existing) {
            if (appointment.getStartTime().isBefore(other.getEndTime()) &&
                    appointment.getEndTime().isAfter(other.getStartTime())) {

                model.addAttribute("error", "This slot overlaps with an existing appointment: " + other.getTitle());
                return "create-appointment";
            }
        }

        // Set the instructor for this appointment
        appointment.setInstructor(currentInstructor);
        appointment.setBooked(false); // ensure new appointments are marked as available

        appointmentRepo.save(appointment);

        return "redirect:/instructor/dashboard";
    }

    @PostMapping("/delete-appointment/{id}")
    public String deleteAppointment(@PathVariable Long id, HttpSession session) {
        // Security Check: Make sure the appointment belongs to the person logged in
        User currentInstructor = (User) session.getAttribute("loggedInUser");
        Optional<Appointment> appointmentOpt = appointmentRepo.findById(id);

        if (appointmentOpt.isPresent()) {
            Appointment appointment = appointmentOpt.get();
            // only delete if the logged-in instructor is the owner
            if (appointment.getInstructor().getId().equals(currentInstructor.getId())) {
                appointmentRepo.delete(appointment);
            }
        }

        return "redirect:/instructor/dashboard";
    }

    @PostMapping("/settings/cancel-window")
    public String updateCancelWindow(@RequestParam int hours, HttpSession session) {
        User instructor = (User) session.getAttribute("loggedInUser");
        instructor.setCancelWindowHours(hours);
        userRepository.save(instructor); // userRepository injected

        // update the session object too so it stays current
        session.setAttribute("loggedInUser", instructor);

        return "redirect:/instructor/dashboard?success=windowUpdated";
    }

    @GetMapping("/create-batch")
    public String showBatchForm() {
        return "instructor/create-batch";
    }

    @PostMapping("/create-batch")
    public String createBatch(BatchAppointmentRequest request, HttpSession session) {
        User instructor = (User) session.getAttribute("loggedInUser");

        LocalDate current = request.getStartDate();
        while (!current.isAfter(request.getEndDate())) {
            // Only create on selected days
            if (request.getDaysOfWeek().contains(current.getDayOfWeek())) {

                LocalTime slotStart = request.getStartTime();
                while (slotStart.plusMinutes(request.getDurationMinutes()).isBefore(request.getEndTime()) ||
                        slotStart.plusMinutes(request.getDurationMinutes()).equals(request.getEndTime())) {

                    Appointment appt = new Appointment();
                    appt.setTitle(request.getTitle());
                    appt.setLocation(request.getLocation());
                    appt.setType(request.getType());
                    appt.setInstructor(instructor);

                    // Combine Date and Time
                    appt.setStartTime(LocalDateTime.of(current, slotStart));
                    appt.setEndTime(LocalDateTime.of(current, slotStart.plusMinutes(request.getDurationMinutes())));

                    appointmentRepo.save(appt);

                    // Move to the next slot (Duration + Buffer)
                    slotStart = slotStart.plusMinutes(request.getDurationMinutes() + request.getBufferMinutes());
                }
            }
            current = current.plusDays(1);
        }
        return "redirect:/instructor/dashboard?success=batchCreated";
    }
}