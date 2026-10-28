package com.sloth.OnlyStudent.application.material;

import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sloth.OnlyStudent.entities.Material;
import com.sloth.OnlyStudent.entities.UserRole;
import com.sloth.OnlyStudent.entities.DTO.MaterialDTO;
import com.sloth.OnlyStudent.repository.MaterialRepository;
import com.sloth.OnlyStudent.shared.errors.MaterialAccessDeniedException;
import com.sloth.OnlyStudent.shared.errors.MaterialNotFoundException;

@Service
public class UpdateMaterialUseCase {

    private final MaterialRepository materialRepository;

    public UpdateMaterialUseCase(MaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }

    @Transactional
    public Material execute(Long materialId, MaterialDTO request, Long principalId, UserRole principalRole) {
        if (principalRole != UserRole.EDUCATOR) {
            throw new MaterialAccessDeniedException();
        }

        Material material = materialRepository.findById(materialId)
                .orElseThrow(MaterialNotFoundException::new);
        if (material.getTurma() == null || material.getTurma().getEducator() == null
                || !Objects.equals(material.getTurma().getEducator().getId(), principalId)) {
            throw new MaterialAccessDeniedException();
        }

        Material validatedMaterial = Material.criar(request.tipo(), request.name(), request.url(),
                request.descricao(), material.getTurma());
        material.setName(validatedMaterial.getName());
        material.setTipo(validatedMaterial.getTipo());
        material.setUrl(validatedMaterial.getUrl());
        material.setDescricao(validatedMaterial.getDescricao());

        return materialRepository.save(material);
    }
}
