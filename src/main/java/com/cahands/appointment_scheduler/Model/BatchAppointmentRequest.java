package com.cahands.appointment_scheduler.Model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import lombok.Data;

@Data
public class BatchAppointmentRequest {
    private String title;
    private String location;
    private Appointment.ApptType type;
    
    private LocalDate startDate;
    private LocalDate endDate;
    private List<DayOfWeek> daysOfWeek;
    
    private LocalTime startTime;
    private LocalTime endTime;
    
    private int durationMinutes;
    private int bufferMinutes;
}