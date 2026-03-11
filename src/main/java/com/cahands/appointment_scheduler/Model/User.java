package com.cahands.appointment_scheduler.Model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data 
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String username;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role; // student/instructor

    public enum Role {
        INSTRUCTOR, STUDENT
    }

    private int cancelWindowHours = 24;
}
