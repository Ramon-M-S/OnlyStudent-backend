package com.sloth.OnlyStudent.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sloth.OnlyStudent.entities.Classroom;
import com.sloth.OnlyStudent.repository.ClassroomRepository;

@Component
public class ClassroomDeletionScheduler {

    private final ClassroomRepository classroomRepository;

    public ClassroomDeletionScheduler(ClassroomRepository classroomRepository) {
        this.classroomRepository = classroomRepository;
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void deleteScheduledClassrooms() {
        List<Classroom> classrooms = classroomRepository
                .findByDataExclusaoAgendadaLessThanEqual(LocalDateTime.now());
        classrooms.forEach(classroom -> {
            if (classroom.getAlunos() != null) {
                classroom.getAlunos().clear();
            }
            classroomRepository.delete(classroom);
        });
    }
}
