package com.sloth.OnlyStudent.application.classroom;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sloth.OnlyStudent.entities.Classroom;
import com.sloth.OnlyStudent.entities.User;
import com.sloth.OnlyStudent.entities.UserRole;
import com.sloth.OnlyStudent.repository.ClassroomRepository;
import com.sloth.OnlyStudent.shared.errors.ClassroomAccessDeniedException;
import com.sloth.OnlyStudent.shared.errors.ClassroomNotFoundException;

@Service
public class CancelClassroomDeletionUseCase {

    private final ClassroomRepository classroomRepository;

    public CancelClassroomDeletionUseCase(ClassroomRepository classroomRepository) {
        this.classroomRepository = classroomRepository;
    }

    @Transactional
    public Classroom execute(Long classroomId, User principal) {
        Classroom classroom = classroomRepository.findByCodigo(classroomId)
                .orElseThrow(ClassroomNotFoundException::new);
        if (principal == null || principal.getRole() != UserRole.EDUCATOR
                || classroom.getEducator() == null
                || !classroom.getEducator().getId().equals(principal.getId())) {
            throw new ClassroomAccessDeniedException();
        }
        classroom.setDataExclusaoAgendada(null);
        return classroomRepository.save(classroom);
    }
}
