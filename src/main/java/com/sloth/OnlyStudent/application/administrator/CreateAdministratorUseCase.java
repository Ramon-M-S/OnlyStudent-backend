package com.sloth.OnlyStudent.application.administrator;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sloth.OnlyStudent.entities.Administrator;
import com.sloth.OnlyStudent.entities.DTO.AdministratorDTO;
import com.sloth.OnlyStudent.entities.DTO.AdministratorResponseDTO;
import com.sloth.OnlyStudent.repository.AdministratorRepository;
import com.sloth.OnlyStudent.repository.UserRepository;
import com.sloth.OnlyStudent.shared.errors.EmailAlreadyExistsException;

@Service
public class CreateAdministratorUseCase {

    private final AdministratorRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final AdministratorResponseMapper mapper;
    private final UserRepository userRepository;

    public CreateAdministratorUseCase(AdministratorRepository repository, PasswordEncoder passwordEncoder,
            AdministratorResponseMapper mapper, UserRepository userRepository) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.mapper = mapper;
        this.userRepository = userRepository;
    }

    @Transactional
    public AdministratorResponseDTO execute(AdministratorDTO request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException();
        }

        Administrator administrator = new Administrator(null, request.name(), request.telephone(), request.email(),
                passwordEncoder.encode(request.password()), com.sloth.OnlyStudent.entities.UserRole.ADMIN);
        try {
            return mapper.toResponse(repository.save(administrator));
        } catch (org.springframework.dao.DataIntegrityViolationException exception) {
            throw new EmailAlreadyExistsException(exception);
        }
    }
}
