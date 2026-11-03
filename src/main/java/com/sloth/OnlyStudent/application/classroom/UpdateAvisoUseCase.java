package com.sloth.OnlyStudent.application.classroom;

import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sloth.OnlyStudent.entities.Aviso;
import com.sloth.OnlyStudent.entities.User;
import com.sloth.OnlyStudent.entities.UserRole;
import com.sloth.OnlyStudent.repository.AvisoRepository;
import com.sloth.OnlyStudent.shared.errors.AvisoAccessDeniedException;
import com.sloth.OnlyStudent.shared.errors.AvisoNotFoundException;

@Service
public class UpdateAvisoUseCase {

    private final AvisoRepository avisoRepository;

    public UpdateAvisoUseCase(AvisoRepository avisoRepository) {
        this.avisoRepository = avisoRepository;
    }

    @Transactional
    public Aviso execute(Long avisoId, String mensagem, User principal) {
        Aviso aviso = avisoRepository.findById(avisoId)
                .orElseThrow(AvisoNotFoundException::new);

        boolean isAuthor = aviso.getAutor() != null
                && Objects.equals(aviso.getAutor().getId(), principal == null ? null : principal.getId());
        boolean isClassroomOwner = principal != null
                && principal.getRole() == UserRole.EDUCATOR
                && aviso.getClassroom() != null
                && aviso.getClassroom().getEducator() != null
                && Objects.equals(aviso.getClassroom().getEducator().getId(), principal.getId());

        if (!isAuthor && !isClassroomOwner) {
            throw new AvisoAccessDeniedException();
        }

        aviso.setMensagem(mensagem);
        return avisoRepository.save(aviso);
    }
}
