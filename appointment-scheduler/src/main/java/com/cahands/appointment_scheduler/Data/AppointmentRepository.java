package com.cahands.appointment_scheduler.Data;

import com.cahands.appointment_scheduler.Model.Appointment;
import org.springframework.data.repository.CrudRepository;
import com.cahands.appointment_scheduler.Model.User;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends CrudRepository<Appointment, Long> {
    // Find all appointments created by a specific instructor
    List<Appointment> findByInstructor(User instructor);

    // Find all appointments booked by a specific instructor
    Optional<Appointment> findById(Long id);

    List<Appointment> findByIsBookedFalse();

    // Find all appointments booked by a specific student
    List<Appointment> findByBookedBy(User student);
}
