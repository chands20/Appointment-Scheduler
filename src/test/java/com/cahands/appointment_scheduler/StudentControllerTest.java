package com.cahands.appointment_scheduler;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

import com.cahands.appointment_scheduler.Data.AppointmentRepository;
import com.cahands.appointment_scheduler.Model.Appointment;
import com.cahands.appointment_scheduler.Model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

@SpringBootTest
@AutoConfigureMockMvc
public class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AppointmentRepository appointmentRepo;

    @Test
    void testCancelBooking_PastDeadline_ReturnsError() throws Exception {
        // Arrange: An appointment that starts in 1 hour, but the instructor has a 24h cancellation policy
        User student = new User();
        student.setId(5L);

        User instructor = new User();
        instructor.setCancelWindowHours(24); // 24 hour policy

        Appointment lateAppt = new Appointment();
        lateAppt.setId(100L);
        lateAppt.setInstructor(instructor);
        lateAppt.setBookedBy(student);
        // Start time is only 1 hour from now (Well within the 24h lockout)
        lateAppt.setStartTime(LocalDateTime.now().plusHours(1));

        when(appointmentRepo.findById(100L)).thenReturn(Optional.of(lateAppt));

        mockMvc.perform(post("/student/cancel/100")
                .sessionAttr("loggedInUser", student))
                .andExpect(redirectedUrl("/student/dashboard?error=tooLateToCancel"));

        verify(appointmentRepo, times(0)).save(any()); // Ensure it wasn't "unbooked"
    }

    @Test
    void testBookGroupAppointment_WithoutGroupId_RedirectsWithError() throws Exception {
        // Arrange: A group appointment that requires a group booking
        User student = new User();
        Appointment groupAppt = new Appointment();
        groupAppt.setId(50L);
        groupAppt.setType(Appointment.ApptType.GROUP);
        groupAppt.setBooked(false);

        when(appointmentRepo.findById(50L)).thenReturn(Optional.of(groupAppt));

        // Act: Try to book without the groupId parameter
        mockMvc.perform(post("/student/book/50")
                .sessionAttr("loggedInUser", student))
                .andExpect(redirectedUrl("/student/dashboard?error=groupRequired"));

        verify(appointmentRepo, times(0)).save(any());
    }
}