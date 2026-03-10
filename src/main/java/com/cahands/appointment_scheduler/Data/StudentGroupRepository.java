package com.cahands.appointment_scheduler.Data;

import com.cahands.appointment_scheduler.Model.StudentGroup;
import com.cahands.appointment_scheduler.Model.User;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface StudentGroupRepository extends CrudRepository<StudentGroup, Long> {
    // Find all groups that a specific student belongs to
    List<StudentGroup> findByMembersContaining(User student);
}
    
