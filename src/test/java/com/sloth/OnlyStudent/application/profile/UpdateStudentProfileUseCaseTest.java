package com.sloth.OnlyStudent.application.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.sloth.OnlyStudent.entities.Student;
import com.sloth.OnlyStudent.entities.UserRole;
import com.sloth.OnlyStudent.entities.DTO.EducatorDTO;
import com.sloth.OnlyStudent.repository.StudentRepository;
import com.sloth.OnlyStudent.shared.errors.ProfileAccessDeniedException;

class UpdateStudentProfileUseCaseTest {

    private final StudentRepository studentRepository = mock(StudentRepository.class);
        private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final UpdateStudentProfileUseCase useCase =
            new UpdateStudentProfileUseCase(studentRepository, new StudentProfileMapper(), passwordEncoder);

    @Test
    void updatesOnlyTheAuthenticatedStudentProfile() {
        Student student = student();
        when(studentRepository.findById(7L)).thenReturn(Optional.of(student));
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = useCase.execute(7L, "ana@example.com",
                new EducatorDTO("Ana Nova", "222", "ignored@example.com", "ignored"));

        assertEquals("Ana Nova", response.orElseThrow().name());
        assertEquals("222", student.getTelephone());
        assertEquals("ana@example.com", student.getEmail());
        verify(studentRepository).save(student);
    }

    @Test
    void rejectsAnUpdateIdentifyingAnotherLogin() {
        when(studentRepository.findById(7L)).thenReturn(Optional.of(student()));

        assertThrows(ProfileAccessDeniedException.class,
                () -> useCase.execute(7L, "other@example.com",
                        new EducatorDTO("Intruso", "222", "other@example.com", null)));
        verify(studentRepository).findById(7L);
        verifyNoMoreInteractions(studentRepository);
    }

    @Test
    void updatesPasswordOnlyWhenProvided() {
        Student student = student();
        when(studentRepository.findById(7L)).thenReturn(Optional.of(student));
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordEncoder.encode("new-password")).thenReturn("encoded-password");

        useCase.execute(7L, null, new EducatorDTO("Ana", "222", null, null, "new-password"));

        assertEquals("encoded-password", student.getPassword());
        verify(passwordEncoder).encode("new-password");
    }

    private Student student() {
        return new Student(7L, "Ana", "111", "ana@example.com", "hash", UserRole.STUDENT);
    }
}
