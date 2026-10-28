package com.sloth.OnlyStudent.entities.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MaterialDTO(
        @NotBlank(message = "O nome do material é obrigatório.")
        @Size(max = 255, message = "O nome do material deve ter no máximo 255 caracteres.")
        String name,
        @NotBlank(message = "O tipo do material é obrigatório.")
        String tipo,
        @NotBlank(message = "A URL do material é obrigatória.")
        String url,
        @NotNull(message = "O código da turma é obrigatório.")
        Long turmaCodigo,
        @Size(max = 500, message = "A descrição do material deve ter no máximo 500 caracteres.")
        String descricao) {

    public MaterialDTO(String name, String tipo, String url, Long turmaCodigo) {
        this(name, tipo, url, turmaCodigo, null);
    }
}
