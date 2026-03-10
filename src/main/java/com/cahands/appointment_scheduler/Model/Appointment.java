package com.cahands.appointment_scheduler.Model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
@Data
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String location;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private ApptType type; // indv/group

    public enum ApptType {
        INDIVIDUAL, GROUP
    }

    private boolean isBooked = false;

    // The student (or group leader) who booked it
    @ManyToOne
    @JoinColumn(name = "booked_by_user_id")
    private User bookedBy;

    // The group that booked it (if it's a group appointment)
    @ManyToOne
    @JoinColumn(name = "booked_group_id")
    private StudentGroup bookedGroup;

    // The instructor who owns the slot
    @ManyToOne
    @JoinColumn(name = "instructor_id")
    private User instructor;

}
