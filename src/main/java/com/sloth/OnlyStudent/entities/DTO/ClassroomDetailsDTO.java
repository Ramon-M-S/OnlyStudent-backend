package com.sloth.OnlyStudent.entities.DTO;

import java.util.List;
import java.util.Set;

public record ClassroomDetailsDTO(
    Long codigo,
    String name,
    String description,
    String educator,
    Long idEducator,
    Set<MaterialResponseDTO> materiais,
    List<String> nomeAlunos,
    List<AvisoResponseDTO> avisos
) {

}