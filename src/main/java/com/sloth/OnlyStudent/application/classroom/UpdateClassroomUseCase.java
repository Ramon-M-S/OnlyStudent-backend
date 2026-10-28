package com.sloth.OnlyStudent.application.classroom;

import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sloth.OnlyStudent.entities.Classroom;
import com.sloth.OnlyStudent.entities.UserRole;
import com.sloth.OnlyStudent.entities.DTO.ClassroomDTO;
import com.sloth.OnlyStudent.repository.ClassroomRepository;
import com.sloth.OnlyStudent.shared.errors.ClassroomAccessDeniedException;
import com.sloth.OnlyStudent.shared.errors.ClassroomNotFoundException;
import com.sloth.OnlyStudent.shared.errors.InvalidClassroomException;

@Service
public class UpdateClassroomUseCase {

    private final ClassroomRepository classroomRepository;

    public UpdateClassroomUseCase(ClassroomRepository classroomRepository) {
        this.classroomRepository = classroomRepository;
    }

    @Transactional
    public Classroom execute(Long classroomId, ClassroomDTO request, Long principalId, UserRole principalRole) {
        if (principalRole != UserRole.EDUCATOR) {
            throw new ClassroomAccessDeniedException();
        }

        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(ClassroomNotFoundException::new);
        if (classroom.getEducator() == null || !Objects.equals(classroom.getEducator().getId(), principalId)) {
            throw new ClassroomAccessDeniedException();
        }

        String newName = request == null ? null : request.name();
        if (newName == null || newName.isBlank()) {
            throw new InvalidClassroomException("O nome da turma é obrigatório.");
        }
        if (newName.length() > 100) {
            throw new InvalidClassroomException("O nome da turma deve ter no máximo 100 caracteres.");
        }

        classroom.setName(newName);
        classroom.setDescription(request.description());
        return classroomRepository.save(classroom);
    }
}
