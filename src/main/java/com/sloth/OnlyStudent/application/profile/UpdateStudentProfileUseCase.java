package com.sloth.OnlyStudent.application.profile;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.sloth.OnlyStudent.entities.Student;
import com.sloth.OnlyStudent.entities.DTO.EducatorDTO;
import com.sloth.OnlyStudent.entities.DTO.StudentResponseDTO;
import com.sloth.OnlyStudent.repository.StudentRepository;
import com.sloth.OnlyStudent.shared.errors.ProfileAccessDeniedException;

@Service
public class UpdateStudentProfileUseCase {

    private final StudentRepository studentRepository;
    private final StudentProfileMapper mapper;
    private final PasswordEncoder passwordEncoder;

    public UpdateStudentProfileUseCase(StudentRepository studentRepository, StudentProfileMapper mapper,
            PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Optional<StudentResponseDTO> execute(Long principalId, String requestedLogin, EducatorDTO request) {
        Optional<Student> optionalStudent = studentRepository.findById(principalId);
        if (optionalStudent.isEmpty()) {
            return Optional.empty();
        }

        Student student = optionalStudent.get();
        if (requestedLogin != null && !requestedLogin.equals(student.getEmail())) {
            throw new ProfileAccessDeniedException();
        }
        student.setName(request.name());
        student.setTelephone(request.telephone());
        if (request.password() != null && !request.password().isBlank()) {
            student.setPassword(passwordEncoder.encode(request.password()));
        }
        return Optional.of(mapper.toResponse(studentRepository.save(student)));
    }
}
