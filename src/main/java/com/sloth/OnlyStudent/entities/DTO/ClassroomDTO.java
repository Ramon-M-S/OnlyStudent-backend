package com.sloth.OnlyStudent.entities.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ClassroomDTO(
        @NotBlank(message = "O nome da turma é obrigatório.")
        @Size(max = 100, message = "O nome da turma deve ter no máximo 100 caracteres.")
        String name,
        String description,
        Long idEducator,
        @PositiveOrZero(message = "O preço da turma não pode ser negativo.")
        double price,
        String coverImageUrl,
        String profileImageUrl,
        @Size(max = 50, message = "A cor da turma deve ter no máximo 50 caracteres.")
        String themeColor) {

    public ClassroomDTO(String name, String description, Long idEducator, double price) {
        this(name, description, idEducator, price, null, null, null);
    }
}
