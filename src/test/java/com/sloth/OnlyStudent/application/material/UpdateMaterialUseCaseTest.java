package com.sloth.OnlyStudent.application.material;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sloth.OnlyStudent.entities.Classroom;
import com.sloth.OnlyStudent.entities.Educator;
import com.sloth.OnlyStudent.entities.Material;
import com.sloth.OnlyStudent.entities.UserRole;
import com.sloth.OnlyStudent.entities.DTO.MaterialDTO;
import com.sloth.OnlyStudent.repository.MaterialRepository;
import com.sloth.OnlyStudent.shared.errors.MaterialAccessDeniedException;
import com.sloth.OnlyStudent.shared.errors.MaterialNotFoundException;

@ExtendWith(MockitoExtension.class)
class UpdateMaterialUseCaseTest {

    @Mock
    private MaterialRepository materialRepository;
    @InjectMocks
    private UpdateMaterialUseCase useCase;

    @Test
    void ownerEducatorUpdatesMaterial() {
        Material material = materialForEducator(7L);
        when(materialRepository.findById(19L)).thenReturn(Optional.of(material));
        when(materialRepository.save(material)).thenReturn(material);

        Material updated = useCase.execute(19L,
                new MaterialDTO("Lista revisada", "pdf", "https://example.com/revised", 12L, "Nova descrição"),
                7L, UserRole.EDUCATOR);

        assertEquals(material, updated);
        assertEquals("Lista revisada", updated.getName());
        assertEquals("https://example.com/revised", updated.getUrl());
        assertEquals("Nova descrição", updated.getDescricao());
        verify(materialRepository).save(material);
    }

    @Test
    void rejectsNonEducator() {
        assertThrows(MaterialAccessDeniedException.class, () -> useCase.execute(19L,
                validRequest(), 7L, UserRole.STUDENT));
    }

    @Test
    void rejectsEducatorWhoDoesNotOwnTheClassroom() {
        when(materialRepository.findById(19L)).thenReturn(Optional.of(materialForEducator(7L)));

        assertThrows(MaterialAccessDeniedException.class, () -> useCase.execute(19L,
                validRequest(), 8L, UserRole.EDUCATOR));
    }

    @Test
    void rejectsMissingMaterial() {
        when(materialRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(MaterialNotFoundException.class, () -> useCase.execute(99L,
                validRequest(), 7L, UserRole.EDUCATOR));
    }

    private MaterialDTO validRequest() {
        return new MaterialDTO("Lista revisada", "pdf", "https://example.com/revised", 12L);
    }

    private Material materialForEducator(Long educatorId) {
        Educator educator = new Educator(educatorId, "Educator", "111", "educator@example.com",
                "hash", UserRole.EDUCATOR, "Matemática");
        Classroom classroom = Classroom.criar("Álgebra", "Aula", educator, 0);
        return Material.criar("PDF", "Apostila", "https://example.com/a.pdf", classroom);
    }
}
