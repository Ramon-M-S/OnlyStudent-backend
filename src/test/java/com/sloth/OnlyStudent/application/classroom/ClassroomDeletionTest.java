package com.sloth.OnlyStudent.application.classroom;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sloth.OnlyStudent.entities.Classroom;
import com.sloth.OnlyStudent.entities.Custo;
import com.sloth.OnlyStudent.entities.Educator;
import com.sloth.OnlyStudent.entities.Status;
import com.sloth.OnlyStudent.entities.User;
import com.sloth.OnlyStudent.entities.UserRole;
import com.sloth.OnlyStudent.repository.ClassroomRepository;
import com.sloth.OnlyStudent.services.ClassroomDeletionScheduler;

@ExtendWith(MockitoExtension.class)
class ClassroomDeletionTest {

    @Mock
    private ClassroomRepository classroomRepository;

    @Test
    void schedulesDeletionAndPublishesNoticeForTheOwner() {
        Classroom classroom = classroom(12L);
        User owner = user(3L);
        when(classroomRepository.findByCodigo(12L)).thenReturn(Optional.of(classroom));
        when(classroomRepository.save(classroom)).thenReturn(classroom);

        new ScheduleClassroomDeletionUseCase(classroomRepository).execute(12L, owner);

        assertNotNull(classroom.getDataExclusaoAgendada());
        assertTrue(classroom.getDataExclusaoAgendada().isAfter(LocalDateTime.now().plusDays(2)));
        assertTrue(classroom.getDataExclusaoAgendada().isBefore(LocalDateTime.now().plusDays(4)));
        assertTrue(classroom.getAvisos().get(0).getMensagem().contains("será excluída permanentemente"));
        verify(classroomRepository).save(classroom);
    }

    @Test
    void cancelsPendingDeletionForTheOwner() {
        Classroom classroom = classroom(12L);
        classroom.setDataExclusaoAgendada(LocalDateTime.now().plusDays(3));
        when(classroomRepository.findByCodigo(12L)).thenReturn(Optional.of(classroom));
        when(classroomRepository.save(classroom)).thenReturn(classroom);

        new CancelClassroomDeletionUseCase(classroomRepository).execute(12L, user(3L));

        assertNull(classroom.getDataExclusaoAgendada());
        verify(classroomRepository).save(classroom);
    }

    @Test
    void deletesClassroomsWhoseGracePeriodHasExpired() {
        Classroom classroom = classroom(12L);
        classroom.setDataExclusaoAgendada(LocalDateTime.now().minusSeconds(1));
        when(classroomRepository.findByDataExclusaoAgendadaLessThanEqual(any(LocalDateTime.class)))
                .thenReturn(List.of(classroom));

        new ClassroomDeletionScheduler(classroomRepository).deleteScheduledClassrooms();

        verify(classroomRepository).delete(classroom);
    }

    private Classroom classroom(Long id) {
        Classroom classroom = new Classroom(id, "Álgebra", "Aula", Status.ATIVA, Custo.GRATUITA, 0,
                new HashSet<>(), new Educator(3L, "Edu", "123", "edu@example.com", "hash",
                        UserRole.EDUCATOR, "Matemática"), new HashSet<>());
        classroom.setAvisos(new ArrayList<>());
        return classroom;
    }

    private User user(Long id) {
        return new User(id, "Edu", "123", "edu@example.com", "hash", UserRole.EDUCATOR);
    }
}
