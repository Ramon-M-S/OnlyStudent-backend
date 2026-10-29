package com.sloth.OnlyStudent.application.classroom;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sloth.OnlyStudent.entities.Aviso;
import com.sloth.OnlyStudent.entities.Classroom;
import com.sloth.OnlyStudent.entities.User;
import com.sloth.OnlyStudent.entities.UserRole;
import com.sloth.OnlyStudent.repository.ClassroomRepository;
import com.sloth.OnlyStudent.shared.errors.ClassroomAccessDeniedException;
import com.sloth.OnlyStudent.shared.errors.ClassroomNotFoundException;

@Service
public class ScheduleClassroomDeletionUseCase {

    private static final DateTimeFormatter NOTICE_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ClassroomRepository classroomRepository;

    public ScheduleClassroomDeletionUseCase(ClassroomRepository classroomRepository) {
        this.classroomRepository = classroomRepository;
    }

    @Transactional
    public Classroom execute(Long classroomId, User principal) {
        Classroom classroom = classroomRepository.findByCodigo(classroomId)
                .orElseThrow(ClassroomNotFoundException::new);
        requireOwner(classroom, principal);

        if (classroom.getDataExclusaoAgendada() == null) {
            LocalDateTime deletionDate = LocalDateTime.now().plusDays(3);
            classroom.setDataExclusaoAgendada(deletionDate);
            classroom.getAvisos().add(new Aviso(
                    "Atenção: Esta turma será excluída permanentemente no dia "
                            + deletionDate.format(NOTICE_DATE_FORMAT)
                            + ". O professor pode cancelar a exclusão até lá.",
                    classroom));
            classroomRepository.save(classroom);
        }
        return classroom;
    }

    private void requireOwner(Classroom classroom, User principal) {
        if (principal == null || principal.getRole() != UserRole.EDUCATOR
                || classroom.getEducator() == null
                || !classroom.getEducator().getId().equals(principal.getId())) {
            throw new ClassroomAccessDeniedException();
        }
    }
}
