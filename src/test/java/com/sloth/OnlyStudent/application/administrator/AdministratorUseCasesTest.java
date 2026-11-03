package com.sloth.OnlyStudent.application.administrator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.sloth.OnlyStudent.entities.Administrator;
import com.sloth.OnlyStudent.entities.UserRole;
import com.sloth.OnlyStudent.entities.DTO.AdministratorDTO;
import com.sloth.OnlyStudent.entities.DTO.AdministratorUpdateDTO;
import com.sloth.OnlyStudent.repository.AdministratorRepository;
import com.sloth.OnlyStudent.repository.UserRepository;
import com.sloth.OnlyStudent.shared.errors.EmailAlreadyExistsException;

class AdministratorUseCasesTest {

    private final AdministratorRepository repository = mock(AdministratorRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final AdministratorResponseMapper mapper = new AdministratorResponseMapper();

    @Test
    void listsAdministratorsAsDtosWithLegacyFieldsAndNoPassword() {
        when(repository.findAll()).thenReturn(List.of(administrator(3L)));

        var response = new ListAdministratorsUseCase(repository, mapper).execute();

        assertEquals(1, response.size());
        assertEquals("admin@example.com", response.get(0).email());
        assertEquals(UserRole.ADMIN, response.get(0).role());
        assertEquals(UserRole.ADMIN, response.get(0).roles());
    }

    @Test
    void getsAdministratorWhenPresent() {
        when(repository.findById(3L)).thenReturn(Optional.of(administrator(3L)));

        assertEquals("Admin", new GetAdministratorUseCase(repository, mapper)
                .execute(3L).orElseThrow().name());
    }

    @Test
    void returnsEmptyWhenAdministratorDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertTrue(new GetAdministratorUseCase(repository, mapper).execute(99L).isEmpty());
    }

    @Test
    void createsAdministratorWithEncodedPasswordAndMapsResponse() {
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(encoder.encode("secret1")).thenReturn("bcrypt-hash");
        when(userRepository.existsByEmail("admin@example.com")).thenReturn(false);
        when(repository.save(any(Administrator.class))).thenAnswer(invocation -> {
            Administrator saved = invocation.getArgument(0);
            saved.setId(7L);
            return saved;
        });

        var response = new CreateAdministratorUseCase(repository, encoder, mapper, userRepository)
            .execute(createRequest());

        assertEquals(7L, response.id());
        assertEquals(UserRole.ADMIN, response.role());
        verify(encoder).encode("secret1");
        verify(repository).save(any(Administrator.class));
    }

    @Test
    void updatesOnlyTheFieldsPreviouslyUpdatedAndReturnsEmptyWhenMissing() {
        Administrator administrator = administrator(3L);
        when(repository.findById(3L)).thenReturn(Optional.of(administrator));
        when(userRepository.findByEmail("new@example.com")).thenReturn(null);
        when(repository.findById(99L)).thenReturn(Optional.empty());
        when(repository.save(any(Administrator.class))).thenAnswer(invocation -> invocation.getArgument(0));
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(encoder.encode("new-password")).thenReturn("new-bcrypt-hash");
        UpdateAdministratorUseCase useCase = new UpdateAdministratorUseCase(repository, mapper, userRepository, encoder);

        var response = useCase.execute(3L, new AdministratorUpdateDTO("Novo", "new@example.com", "555",
            "new-password"));

        assertEquals("new@example.com", administrator.getEmail());
        assertEquals("Novo", administrator.getName());
        assertEquals("555", administrator.getTelephone());
        assertEquals("new-bcrypt-hash", administrator.getPassword());
        assertTrue(response.isPresent());
        assertTrue(useCase.execute(99L, updateRequest()).isEmpty());
    }

        @Test
        void rejectsDuplicateEmailWhenCreatingAdministrator() {
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);
        PasswordEncoder encoder = mock(PasswordEncoder.class);

        assertThrows(EmailAlreadyExistsException.class,
            () -> new CreateAdministratorUseCase(repository, encoder, mapper, userRepository)
                .execute(new AdministratorDTO("Admin", "existing@example.com", "secret1", "111")));
        }

        @Test
        void rejectsDuplicateEmailWhenUpdatingAdministrator() {
        when(repository.findById(3L)).thenReturn(Optional.of(administrator(3L)));
        when(userRepository.findByEmail("existing@example.com"))
            .thenReturn(new Administrator(8L, "Other", "222", "existing@example.com", "hash", UserRole.ADMIN));
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        UpdateAdministratorUseCase useCase = new UpdateAdministratorUseCase(repository, mapper, userRepository, encoder);

        assertThrows(EmailAlreadyExistsException.class,
            () -> useCase.execute(3L,
                new AdministratorUpdateDTO("Admin", "existing@example.com", "111", null)));
        }

    @Test
    void deletesExistingAdministratorAndReportsMissingId() {
        when(repository.findById(3L)).thenReturn(Optional.of(administrator(3L)));
        when(repository.findById(99L)).thenReturn(Optional.empty());
        DeleteAdministratorUseCase useCase = new DeleteAdministratorUseCase(repository);

        assertTrue(useCase.execute(3L));
        assertFalse(useCase.execute(99L));
        verify(repository).delete(any(Administrator.class));
    }

    private Administrator administrator(Long id) {
        return new Administrator(id, "Admin", "111", "admin@example.com", "old-hash", UserRole.ADMIN);
    }

    private AdministratorDTO createRequest() {
        return new AdministratorDTO("Admin", "admin@example.com", "secret1", "111");
    }

    private AdministratorUpdateDTO updateRequest() {
        return new AdministratorUpdateDTO("Admin", "admin@example.com", "111", null);
    }
}
