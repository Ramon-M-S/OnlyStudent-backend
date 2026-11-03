package com.sloth.OnlyStudent.application.administrator;

import java.util.Optional;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.sloth.OnlyStudent.entities.Administrator;
import com.sloth.OnlyStudent.entities.User;
import com.sloth.OnlyStudent.entities.DTO.AdministratorResponseDTO;
import com.sloth.OnlyStudent.entities.DTO.AdministratorUpdateDTO;
import com.sloth.OnlyStudent.repository.AdministratorRepository;
import com.sloth.OnlyStudent.repository.UserRepository;
import com.sloth.OnlyStudent.shared.errors.EmailAlreadyExistsException;

@Service
public class UpdateAdministratorUseCase {

    private final AdministratorRepository repository;
    private final AdministratorResponseMapper mapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UpdateAdministratorUseCase(AdministratorRepository repository, AdministratorResponseMapper mapper,
            UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.mapper = mapper;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Optional<AdministratorResponseDTO> execute(Long id, AdministratorUpdateDTO request) {
        Optional<Administrator> optionalAdministrator = repository.findById(Objects.requireNonNull(id, "id"));
        if (optionalAdministrator.isEmpty()) {
            return Optional.empty();
        }

        Administrator administrator = optionalAdministrator.get();
        User existingUser = userRepository.findByEmail(request.email());
        if (existingUser != null && !Objects.equals(existingUser.getId(), id)) {
            throw new EmailAlreadyExistsException();
        }
        administrator.setName(request.name());
        administrator.setEmail(request.email());
        administrator.setTelephone(request.telephone());
        if (request.password() != null && !request.password().isBlank()) {
            administrator.setPassword(passwordEncoder.encode(request.password()));
        }
        try {
            return Optional.of(mapper.toResponse(repository.save(administrator)));
        } catch (org.springframework.dao.DataIntegrityViolationException exception) {
            throw new EmailAlreadyExistsException(exception);
        }
    }
}
