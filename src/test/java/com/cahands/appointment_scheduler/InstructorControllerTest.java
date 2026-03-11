package com.cahands.appointment_scheduler;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.cahands.appointment_scheduler.Data.AppointmentRepository;
import com.cahands.appointment_scheduler.Model.Appointment;
import com.cahands.appointment_scheduler.Model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

@SpringBootTest
@AutoConfigureMockMvc
public class InstructorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AppointmentRepository appointmentRepo;

    private User mockInstructor;

    @BeforeEach
    void setUp() {
        mockInstructor = new User();
        mockInstructor.setId(1L);
        mockInstructor.setName("Dr. Smith");
    }

    @Test
    void testSaveAppointment_OverlappingSlot_ReturnsError() throws Exception {
        // Setup: One appointment already exists from 2pm to 3pm
        Appointment existing = new Appointment();
        existing.setStartTime(LocalDateTime.now().plusDays(1).withHour(14).withMinute(0));
        existing.setEndTime(LocalDateTime.now().plusDays(1).withHour(15).withMinute(0));
        
        when(appointmentRepo.findByInstructor(any())).thenReturn(Collections.singletonList(existing));

        // Act & Assert: Try to save a new appointment from 2:30pm to 3:30pm  (overlap)
        mockMvc.perform(post("/instructor/create-appointment")
                .sessionAttr("loggedInUser", mockInstructor)
                .param("startTime", LocalDateTime.now().plusDays(1).withHour(14).withMinute(30).toString())
                .param("endTime", LocalDateTime.now().plusDays(1).withHour(15).withMinute(30).toString())
                .param("title", "Overlapping Session"))
                .andExpect(model().attributeExists("error"))
                .andExpect(view().name("create-appointment"));
        
        verify(appointmentRepo, times(0)).save(any()); // Ensure it was NEVER saved
    }

    @Test
    void testDeleteAppointment_UnauthorizedUser_DoesNotDelete() throws Exception {
        // Setup: Appointment belongs to Instructor 99, but Instructor 1 is logged in
        User differentInstructor = new User();
        differentInstructor.setId(99L);
        
        Appointment target = new Appointment();
        target.setId(10L);
        target.setInstructor(differentInstructor);

        when(appointmentRepo.findById(10L)).thenReturn(Optional.of(target));

        // Act
        mockMvc.perform(post("/instructor/delete-appointment/10")
                .sessionAttr("loggedInUser", mockInstructor))
                .andExpect(redirectedUrl("/instructor/dashboard"));

        // Assert: delete should never be called because IDs don't match
        verify(appointmentRepo, times(0)).delete(any());
    }
}