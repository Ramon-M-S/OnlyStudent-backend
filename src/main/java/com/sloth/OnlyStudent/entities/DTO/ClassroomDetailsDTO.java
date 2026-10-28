package com.sloth.OnlyStudent.entities.DTO;

import java.time.LocalDateTime;
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
    List<AvisoResponseDTO> avisos,
    LocalDateTime dataExclusaoAgendada
) {

    public ClassroomDetailsDTO(Long codigo, String name, String description, String educator, Long idEducator,
            Set<MaterialResponseDTO> materiais, List<String> nomeAlunos, List<AvisoResponseDTO> avisos) {
        this(codigo, name, description, educator, idEducator, materiais, nomeAlunos, avisos, null);
    }
}