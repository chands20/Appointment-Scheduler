package com.cahands.appointment_scheduler.Controllers;

import com.cahands.appointment_scheduler.Data.AppointmentRepository;
import com.cahands.appointment_scheduler.Data.StudentGroupRepository;
import com.cahands.appointment_scheduler.Model.Appointment;
import com.cahands.appointment_scheduler.Model.StudentGroup;
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
    private final StudentGroupRepository groupRepo;

    public StudentController(AppointmentRepository appointmentRepo, StudentGroupRepository groupRepo) {
        this.appointmentRepo = appointmentRepo;
        this.groupRepo = groupRepo;
    }

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        User student = (User) session.getAttribute("loggedInUser");
        // SAFETY CHECK: If session timed out or user isn't found, send to login
        if (student == null) {
            return "redirect:/login";
        }

        // List 1: Everything available to book
        model.addAttribute("availableAppointments", appointmentRepo.findByIsBookedFalse());

        // List 2: what this student has booked
        model.addAttribute("myBookings", appointmentRepo.findByBookedBy(student));

        model.addAttribute("myGroups", groupRepo.findByMembersContaining(student));

        // ALL groups (for the "Join" dropdown)
        model.addAttribute("allGroups", groupRepo.findAll());

        return "student-dashboard";
    }

    @PostMapping("/groups/create")
    public String createGroup(@RequestParam String groupName, HttpSession session) {
        User student = (User) session.getAttribute("loggedInUser");

        StudentGroup newGroup = new StudentGroup();
        newGroup.setGroupName(groupName);
        newGroup.getMembers().add(student); // Creator is the first member

        groupRepo.save(newGroup);
        return "redirect:/student/dashboard?success=groupCreated";
    }

    @PostMapping("/groups/join")
    public String joinGroup(@RequestParam Long groupId, HttpSession session) {
        User student = (User) session.getAttribute("loggedInUser");
        StudentGroup group = groupRepo.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        // Add student to group if they aren't already in it
        if (!group.getMembers().contains(student)) {
            group.getMembers().add(student);
            groupRepo.save(group);
        }

        return "redirect:/student/dashboard?success=joinedGroup";
    }

    @PostMapping("/groups/leave/{id}")
    public String leaveGroup(@PathVariable Long id, HttpSession session) {
        User student = (User) session.getAttribute("loggedInUser");
        StudentGroup group = groupRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        // remove the student from the members list
        group.getMembers().removeIf(m -> m.getId().equals(student.getId()));

        if (group.getMembers().isEmpty()) {
            // If no one is left, delete the group
            groupRepo.delete(group);
        } else {
            // Otherwise just save the update
            groupRepo.save(group);
        }

        return "redirect:/student/dashboard?info=leftGroup";
    }

    @PostMapping("/book/{id}")
    public String bookAppointment(@PathVariable Long id, @RequestParam(required = false) Long groupId,
            HttpSession session) {
        User student = (User) session.getAttribute("loggedInUser");
        Optional<Appointment> apptOpt = appointmentRepo.findById(id);

        if (apptOpt.isPresent()) {
            Appointment appt = apptOpt.get();

            if (!appt.isBooked()) {
                // 3. Handle Group vs Individual Logic
                if (appt.getType() == Appointment.ApptType.GROUP) {
                    if (groupId == null) {
                        return "redirect:/student/dashboard?error=groupRequired";
                    }
                    StudentGroup group = groupRepo.findById(groupId)
                            .orElseThrow(() -> new IllegalArgumentException("Invalid Group ID"));
                    appt.setBookedGroup(group);
                }

                appt.setBooked(true);
                appt.setBookedBy(student); // The individual who clicked the button
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