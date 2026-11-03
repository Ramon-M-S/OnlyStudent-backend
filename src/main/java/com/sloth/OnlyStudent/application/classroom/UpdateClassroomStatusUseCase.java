package com.sloth.OnlyStudent.application.classroom;

import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sloth.OnlyStudent.entities.Classroom;
import com.sloth.OnlyStudent.entities.Status;
import com.sloth.OnlyStudent.entities.User;
import com.sloth.OnlyStudent.entities.UserRole;
import com.sloth.OnlyStudent.repository.ClassroomRepository;
import com.sloth.OnlyStudent.shared.errors.ClassroomAccessDeniedException;
import com.sloth.OnlyStudent.shared.errors.ClassroomNotFoundException;

@Service
public class UpdateClassroomStatusUseCase {

    private final ClassroomRepository classroomRepository;

    public UpdateClassroomStatusUseCase(ClassroomRepository classroomRepository) {
        this.classroomRepository = classroomRepository;
    }

    @Transactional
    public Classroom execute(Long classroomId, Status status, User principal) {
        if (principal == null || principal.getRoles() != UserRole.EDUCATOR) {
            throw new ClassroomAccessDeniedException();
        }

        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(ClassroomNotFoundException::new);
        if (classroom.getEducator() == null
                || !Objects.equals(classroom.getEducator().getId(), principal.getId())) {
            throw new ClassroomAccessDeniedException();
        }

        classroom.setStatus(status);
        return classroomRepository.save(classroom);
    }
}